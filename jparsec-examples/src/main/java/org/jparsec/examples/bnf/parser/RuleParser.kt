/*****************************************************************************
 * Copyright (C) jparsec.org                                                *
 * ------------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License");           *
 * you may not use this file except in compliance with the License.          *
 * You may obtain a copy of the License at                                   *
 * *
 * http://www.apache.org/licenses/LICENSE-2.0                                *
 * *
 * Unless required by applicable law or agreed to in writing, software       *
 * distributed under the License is distributed on an "AS IS" BASIS,         *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
 * See the License for the specific language governing permissions and       *
 * limitations under the License.                                            *
 */
package org.jparsec.examples.bnf.parser

import org.jparsec.Parser
import org.jparsec.Parser.Companion.newReference
import org.jparsec.Parsers.or
import org.jparsec.Parsers.sequence
import org.jparsec.Terminals
import org.jparsec.examples.bnf.ast.*
import java.util.function.Function

/**
 * Parser for bnf rules.
 *
 * @author benyu
 */
object RuleParser {
    val LITERAL = Terminals.StringLiteral.PARSER.map(Function<String, Rule> { literal: String -> LiteralRule(literal) })
    val IDENT: Parser<Rule> = Terminals.Identifier.PARSER.notFollowedBy(TerminalParser.term("::="))
        .map(Function { name: String? -> RuleReference(name!!) })
    var RULE_DEF = sequence(Terminals.Identifier.PARSER, TerminalParser.term("::="), rule()) { name: String?, _: Any?, r: Rule? -> RuleDef(name!!, r!!) }
    var RULE_DEFS = RULE_DEF.many()

    fun rule(): Parser<Rule> {
        val ref: Parser.Reference<Rule> = newReference()
        val atom: Parser<Rule> = or(LITERAL, IDENT, unit(ref.lazy()))
        val parser = alternative(sequential(atom))
        ref.set(parser)
        return parser
    }

    fun unit(rule: Parser<Rule>): Parser<Rule> {
        return or(
            rule.between(TerminalParser.term("("), TerminalParser.term(")")),
            rule.between(TerminalParser.INDENTATION.indent(), TerminalParser.INDENTATION.outdent()))
    }

    fun sequential(rule: Parser<Rule>): Parser<Rule> {
        return rule.many1().map(Function { list: List<Rule> -> if (list.size == 1) list[0] else SequentialRule(list) })
    }

    fun alternative(rule: Parser<Rule>): Parser<Rule> {
        return rule.sepBy1(TerminalParser.term("|"))
            .map(Function { list: List<Rule> -> if (list.size == 1) list[0] else AltRule(list) })
    }
}