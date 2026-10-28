package com.example.slisaconnect.data


/**
 * A small JSON reader and writer so saving needs no extra library.
 * Writes Map, List, String, Boolean, Int, Long, Double and null. Reads them back as Map, List, String, Boolean, Long, Double and null.
 */
object Json {
    fun write(value: Any?): String = StringBuilder().also { append(it, value) }.toString()

    private fun append(sb: StringBuilder, v: Any?) {
        when (v) {
            null -> sb.append("null")
            is String -> quote(sb, v)
            is Boolean -> sb.append(if (v) "true" else "false")
            is Int, is Long -> sb.append(v.toString())
            is Double -> sb.append(v.toString())
            is Map<*, *> -> {
                sb.append('{')
                var first = true
                for ((k, x) in v) {
                    if (!first) sb.append(',')
                    first = false
                    quote(sb, k.toString())
                    sb.append(':')
                    append(sb, x)
                }
                sb.append('}')
            }
            is Iterable<*> -> {
                sb.append('[')
                var first = true
                for (x in v) {
                    if (!first) sb.append(',')
                    first = false
                    append(sb, x)
                }
                sb.append(']')
            }
            else -> quote(sb, v.toString())
        }
    }

    private fun quote(sb: StringBuilder, s: String) {
        sb.append('"')
        for (c in s) {
            when (c) {
                '"' -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> if (c < ' ') sb.append(String.format("\\u%04x", c.code)) else sb.append(c)
            }
        }
        sb.append('"')
    }

    fun parse(text: String): Any? {
        val p = Parser(text)
        val v = p.readValue()
        p.skipSpace()
        require(p.atEnd()) { "Unexpected text after the JSON value." }
        return v
    }

    private class Parser(private val s: String) {
        private var i = 0

        fun atEnd() = i >= s.length

        fun skipSpace() {
            while (i < s.length && s[i].isWhitespace()) i++
        }

        fun readValue(): Any? {
            skipSpace()
            require(i < s.length) { "Unexpected end of JSON." }
            return when (s[i]) {
                '{' -> readObject()
                '[' -> readArray()
                '"' -> readString()
                't' -> literal("true", true)
                'f' -> literal("false", false)
                'n' -> literal("null", null)
                else -> readNumber()
            }
        }

        private fun literal(word: String, value: Any?): Any? {
            require(s.startsWith(word, i)) { "Unexpected text at $i." }
            i += word.length
            return value
        }

        private fun readObject(): Map<String, Any?> {
            val out = LinkedHashMap<String, Any?>()
            i++ // {
            skipSpace()
            if (i < s.length && s[i] == '}') { i++; return out }
            while (true) {
                skipSpace()
                require(i < s.length && s[i] == '"') { "Expected a key at $i." }
                val key = readString()
                skipSpace()
                require(i < s.length && s[i] == ':') { "Expected : at $i." }
                i++
                out[key] = readValue()
                skipSpace()
                require(i < s.length) { "Unexpected end of JSON." }
                if (s[i] == ',') { i++; continue }
                require(s[i] == '}') { "Expected } at $i." }
                i++
                return out
            }
        }

        private fun readArray(): List<Any?> {
            val out = ArrayList<Any?>()
            i++ // [
            skipSpace()
            if (i < s.length && s[i] == ']') { i++; return out }
            while (true) {
                out.add(readValue())
                skipSpace()
                require(i < s.length) { "Unexpected end of JSON." }
                if (s[i] == ',') { i++; continue }
                require(s[i] == ']') { "Expected ] at $i." }
                i++
                return out
            }
        }

        private fun readString(): String {
            val sb = StringBuilder()
            i++ // opening quote
            while (true) {
                require(i < s.length) { "Unterminated string." }
                val c = s[i++]
                when (c) {
                    '"' -> return sb.toString()
                    '\\' -> {
                        require(i < s.length) { "Unterminated escape." }
                        when (val e = s[i++]) {
                            '"' -> sb.append('"')
                            '\\' -> sb.append('\\')
                            '/' -> sb.append('/')
                            'b' -> sb.append('\b')
                            'f' -> sb.append('\u000C')
                            'n' -> sb.append('\n')
                            'r' -> sb.append('\r')
                            't' -> sb.append('\t')
                            'u' -> {
                                require(i + 4 <= s.length) { "Bad unicode escape." }
                                sb.append(s.substring(i, i + 4).toInt(16).toChar())
                                i += 4
                            }
                            else -> throw IllegalArgumentException("Bad escape \\$e.")
                        }
                    }
                    else -> sb.append(c)
                }
            }
        }

        private fun readNumber(): Any {
            val start = i
            while (i < s.length && (s[i].isDigit() || s[i] == '-' || s[i] == '+' || s[i] == '.' || s[i] == 'e' || s[i] == 'E')) i++
            val t = s.substring(start, i)
            require(t.isNotEmpty()) { "Unexpected character at $start." }
            return if (t.any { it == '.' || it == 'e' || it == 'E' }) t.toDouble() else t.toLong()
        }
    }
}
