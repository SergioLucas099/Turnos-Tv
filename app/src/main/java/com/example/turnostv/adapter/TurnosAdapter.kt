package com.example.turnostv.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.turnostv.R
import com.example.turnostv.model.Turnos

class TurnosAdapter (
    private val lista: MutableList<Turnos>,
    private val onEditar: (Turnos) -> Unit
) : RecyclerView.Adapter<TurnosAdapter.TurnosAdapterViewHolder>(){

    inner class TurnosAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNombre: TextView =
            itemView.findViewById(R.id.txtNombre)
        val txtTurno: TextView =
            itemView.findViewById(R.id.txtTurno)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TurnosAdapterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.turnos_item, parent, false)

        return TurnosAdapterViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: TurnosAdapterViewHolder, position: Int) {

        val turno = lista[position]

        holder.txtNombre.text = turno.nombreAtraccion
        holder.txtTurno.text = turno.numeroTurno
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<Turnos>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}