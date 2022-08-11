package co.gov.cnsc.mobile.simo.models

class Tests {
    var id: String            = "";
    var descripcion: String   = "";
    var idMcipio: Int         = 0;
    var nombreMcipio : String = "";
    var idDepto: Int          = 0;
    var nombreDepto : String  = "";

    constructor(id: String, descripcion: String, idMcipio: Int, nombreMcipio: String, idDepto: Int, nombreDepto : String){
        this.id           = id;
        this.descripcion  = descripcion;
        this.idMcipio     = idMcipio;
        this.nombreMcipio = nombreMcipio;
        this.idDepto      = idDepto;
        this.nombreDepto  = nombreDepto;
    }
}