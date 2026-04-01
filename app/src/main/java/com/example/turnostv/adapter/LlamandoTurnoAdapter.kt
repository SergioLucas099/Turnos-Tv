package com.example.turnostv.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.turnostv.R
import com.example.turnostv.model.Turnos

class LlamandoTurnoAdapter (
    private val lista: MutableList<Turnos>,
    private val onEditar: (Turnos) -> Unit
) : RecyclerView.Adapter<LlamandoTurnoAdapter.LlamandoTurnoAdapterViewHolder>(){

    inner class LlamandoTurnoAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val txtNombreTurnoLlamado: TextView =
            itemView.findViewById(R.id.txtNombreTurnoLlamado)
        val txtTurnoLlamado: TextView =
            itemView.findViewById(R.id.txtTurnoLlamado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LlamandoTurnoAdapterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.llamando_turno_item, parent, false)

        return LlamandoTurnoAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: LlamandoTurnoAdapterViewHolder, position: Int) {
        val turno = lista[position]

        holder.txtNombreTurnoLlamado.text = turno.nombreAtraccion
        holder.txtTurnoLlamado.text = turno.numeroTurno
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<Turnos>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}