package core.engine

interface CrawlerTaskFactory<Document : Request> {
    fun <S1 : Transaction<Document>,
            S2 : StrictTransaction<S1, Document>>
            get1(type : DocumentType) : CrawlerTask1<S1, S2, Document, Document>

    fun <S1 : Transaction<Document>,
            S2 : StrictTransaction<S1, Document>,
            S3 : StrictTransaction<S2, Document>>
            get2(type : DocumentType) : CrawlerTask2<S1, S2, S3, Document, Document, Document>


    fun <S1 : Transaction<Document>,
            S2 : StrictTransaction<S1, Document>,
            S3 : StrictTransaction<S2, Document>,
            S4 : StrictTransaction<S3, Document>>
            get3(type : DocumentType) : CrawlerTask3<S1, S2, S3, S4, Document, Document, Document, Document>


    fun <S1 : Transaction<Document>,
            S2 : StrictTransaction<S1, Document>,
            S3 : StrictTransaction<S2, Document>,
            S4 : StrictTransaction<S3, Document>,
            S5 : StrictTransaction<S4, Document>>
            get4(type : DocumentType) : CrawlerTask4<S1, S2, S3, S4, S5, Document, Document, Document, Document, Document>

}

