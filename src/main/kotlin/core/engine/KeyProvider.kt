package core.engine

class KeyProvider (
    val documentKey : DocumentUniqueKeyProvider,
    val tagKey : TagUniqueKeyProvider){
}

interface DocumentUniqueKeyProvider {
    fun <Document : Request> create(doc : Document) : UniqueKey
}

interface TagUniqueKeyProvider {
    fun create(doc : TagRepository) : Iterable<UniqueKey>
}