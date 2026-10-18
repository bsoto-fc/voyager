package mx.unam.ciencias.myp.miner

/*
 * Interfaz utilizada para poder realizar operaciones sin considerar las diferencias entre plataformas.
 */
interface MinerProvider {
    fun recursiveSearch(path: String)
}
