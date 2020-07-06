package norm.model

data class SqlModel(
        val params: List<ParamModel>,
        val cols: List<ColumnModel>,
        val preparableStatement: String
)
