package es.uva.ubicate.data.model;

import java.text.DateFormat;
import java.util.Date;

public class Evento implements Comparable {
    private String titulo, descripcion;
    private String fecha;
    private String idEvento;

    public Evento(){

    }

    public Evento(String titulo, String descripcion, String fecha){
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fecha = fecha;
    }

    public String getIdEvento(){
        return idEvento;
    }

    public void setIdEvento(String idEvento){
        this.idEvento = idEvento;
    }

    public String getTitulo(){
        return titulo;
    }

    public String getDescripcion(){
        return descripcion;
    }

    public String getFecha(){
        return fecha;
    }

    @Override
    public int compareTo(Object o) {
        if(o.getClass()!=getClass())
            return 0;
        else{
            Evento evento = (Evento)o;
            try {
                Date date = DateFormat.getDateInstance(DateFormat.FULL).parse(fecha);
                Date oDate = DateFormat.getDateInstance(DateFormat.FULL).parse(evento.getFecha());
                return date.compareTo(oDate);
            }catch(Exception e){
                return 0;
            }

        }
    }
}
