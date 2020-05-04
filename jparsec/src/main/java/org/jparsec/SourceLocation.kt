package org.jparsec

import org.jparsec.error.Location

/**
 * Represents a location inside the source.
 *
 * *Not thread safe*.
 *
 * @since 3.1
 */
class SourceLocation internal constructor(
    /** Returns the 0-based index within the source.  */
    val index: Int, private val locator: SourceLocator) {
    private var location: Location? = null
        private get() {
            if (field == null) {
                field = locator.locate(index)
            }
            return field
        }

    /**
     * Returns the line number of this location. Because this method takes amortized `log(n)` time,
     * it's typically a good idea to avoid calling it until the entire source has been successfully parsed.
     */
    val line: Int
        get() = location!!.line

    /**
     * Returns the column number of this location. Because this method takes amortized `log(n)` time,
     * it's typically a good idea to avoid calling it until the entire source has been successfully parsed.
     */
    val column: Int
        get() = location!!.column

}