package co.gov.cnsc.mobile.simo.models

class Mncipios {
    var id: String = "";
    var codDane: String = "";
    var nombre: String = "";

    constructor(id: String, codDane: String, nombre: String){
        this.id = id;
        this.codDane = codDane;
        this.nombre = nombre;
    }

    override fun toString(): String {
        return "Mncipios(id='$id', codDane='$codDane', nombre='$nombre')"
    }
}