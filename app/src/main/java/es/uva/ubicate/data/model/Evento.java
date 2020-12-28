package es.uva.ubicate.data.model;

import java.util.Date;

public class Evento {
    private String titulo, descripcion;
    private String fecha;

    public Evento(){

    }

    public Evento(String titulo, String descripcion, String fecha){
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fecha = fecha;
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
}
