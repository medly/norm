package norm.model

data class TableModel(
        val columns: List<ColumnModel>,
        val primaryKeys: List<ColumnModel>
)
