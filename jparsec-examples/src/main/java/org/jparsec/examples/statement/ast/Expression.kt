package org.jparsec.examples.statement.ast

interface Expression
data class DoubleExpression(var s: String, var s2: String): Expression
data class FullExpression(var identExpr: IdentExpression, var valueExpr: ValueExpression): Expression
data class IdentExpression(var s: String): Expression
data class ReadonlyExpression(var s: String): Expression
data class SingleExpression(var s: String): Expression
class ValueExpression(s: String): Expression {
    var nVal: Int = s.toInt()
}
data class VarExpression(val s: String): Expression