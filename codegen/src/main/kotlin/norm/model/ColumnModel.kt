package norm.model

data class ColumnModel(
    val fieldName: String,
    val colType: String,
    val colName: String,
    val isNullable: Boolean
)
