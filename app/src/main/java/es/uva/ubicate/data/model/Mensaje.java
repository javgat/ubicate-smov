package es.uva.ubicate.data.model;

import java.text.DateFormat;
import java.util.Date;

public class Mensaje implements Comparable{
    private String mensaje, idAutor, fecha;

    public Mensaje() {
    }

    public Mensaje(String mensaje, String idAutor, String fecha) {
        this.mensaje = mensaje;
        this.idAutor = idAutor;
        this.fecha = fecha;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getIdAutor(){
        return idAutor;
    }

    public String getFecha(){
        return fecha;
    }


    @Override
    public int compareTo(Object o) {
        if(o.getClass()!=getClass())
            return 0;
        Mensaje otro = (Mensaje)o;
        try {
            Date date = DateFormat.getDateTimeInstance().parse(fecha);
            Date oDate = DateFormat.getDateTimeInstance().parse(otro.getFecha());
            return date.compareTo(oDate);
        }catch(Exception e){
            return 0;
        }
    }
}
