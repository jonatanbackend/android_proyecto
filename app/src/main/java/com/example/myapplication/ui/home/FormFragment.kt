package com.example.myapplication.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class FormFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_form, container, false)

        // Referencias a los campos de entrada
        val etNumeroActa = view.findViewById<EditText>(R.id.etNumeroActa)
        val etNumeroOrden = view.findViewById<EditText>(R.id.etNumeroOrden)
        val etTecnico = view.findViewById<EditText>(R.id.etTecnico)
        val actvEstado = view.findViewById<MaterialAutoCompleteTextView>(R.id.actvEstado)
        val etMotivoRetiro = view.findViewById<EditText>(R.id.etMotivoRetiro)
        val actvClase = view.findViewById<MaterialAutoCompleteTextView>(R.id.actvClase)
        val etMedidor = view.findViewById<EditText>(R.id.etMedidor)
        val etMarca = view.findViewById<EditText>(R.id.etMarca)
        val etSerie = view.findViewById<EditText>(R.id.etSerie)
        val etLectura = view.findViewById<EditText>(R.id.etLectura)
        val etMultiplo = view.findViewById<EditText>(R.id.etMultiplo)

        // Referencias a los botones
        val btnGuardar = view.findViewById<View>(R.id.btnGuardar)
        val btnNuevo = view.findViewById<View>(R.id.btnNuevo)
        val btnExportar = view.findViewById<View>(R.id.btnExportar)

        // Configuración de los MaterialAutoCompleteTextView
        setupAutoCompleteTextView(actvEstado, listOf("Instalado", "Retirado", "Pendiente"))
        setupAutoCompleteTextView(actvClase, listOf("Activa", "Inactiva", "Pendiente"))

        // Lógica para el botón Guardar
        btnGuardar.setOnClickListener {
            guardarDatos(
                etNumeroActa.text.toString(),
                etNumeroOrden.text.toString(),
                etTecnico.text.toString(),
                actvEstado.text.toString(),
                etMotivoRetiro.text.toString(),
                actvClase.text.toString(),
                etMedidor.text.toString(),
                etMarca.text.toString(),
                etSerie.text.toString(),
                etLectura.text.toString(),
                etMultiplo.text.toString()
            )
        }

        // Lógica para el botón Nuevo
        btnNuevo.setOnClickListener {
            reiniciarFormulario(
                etNumeroActa, etNumeroOrden, etTecnico, actvEstado, etMotivoRetiro,
                actvClase, etMedidor, etMarca, etSerie, etLectura, etMultiplo
            )
        }

        // Lógica para el botón Exportar
        btnExportar.setOnClickListener {
            Toast.makeText(requireContext(), "Función de exportar aún no implementada", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    // Configurar un MaterialAutoCompleteTextView con un ArrayAdapter
    private fun setupAutoCompleteTextView(autoCompleteTextView: MaterialAutoCompleteTextView, items: List<String>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items)
        autoCompleteTextView.setAdapter(adapter)

        // Manejar la selección
        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = items[position]
            Toast.makeText(requireContext(), "Seleccionaste: $selectedItem", Toast.LENGTH_SHORT).show()
        }
    }

    // Guardar datos y mostrar un mensaje
    private fun guardarDatos(
        numeroActa: String, numeroOrden: String, tecnico: String,
        estado: String, motivoRetiro: String, clase: String,
        medidor: String, marca: String, serie: String,
        lectura: String, multiplo: String
    ) {
        if (numeroActa.isNotEmpty() && numeroOrden.isNotEmpty() && tecnico.isNotEmpty() &&
            estado.isNotEmpty() && clase.isNotEmpty()
        ) {
            Toast.makeText(
                requireContext(),
                "Datos guardados:\nActa: $numeroActa\nOrden: $numeroOrden\nTécnico: $tecnico\nEstado: $estado\nMotivo: $motivoRetiro\nClase: $clase\nMedidor: $medidor\nMarca: $marca\nSerie: $serie\nLectura: $lectura\nMúltiplo: $multiplo",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(requireContext(), "Por favor, complete todos los campos obligatorios", Toast.LENGTH_SHORT).show()
        }
    }

    // Reiniciar el formulario
    private fun reiniciarFormulario(
        etNumeroActa: EditText, etNumeroOrden: EditText, etTecnico: EditText,
        actvEstado: MaterialAutoCompleteTextView, etMotivoRetiro: EditText,
        actvClase: MaterialAutoCompleteTextView, etMedidor: EditText,
        etMarca: EditText, etSerie: EditText, etLectura: EditText, etMultiplo: EditText
    ) {
        etNumeroActa.text.clear()
        etNumeroOrden.text.clear()
        etTecnico.text.clear()
        actvEstado.text.clear()
        etMotivoRetiro.text.clear()
        actvClase.text.clear()
        etMedidor.text.clear()
        etMarca.text.clear()
        etSerie.text.clear()
        etLectura.text.clear()
        etMultiplo.text.clear()
        Toast.makeText(requireContext(), "Formulario reiniciado", Toast.LENGTH_SHORT).show()
    }
}