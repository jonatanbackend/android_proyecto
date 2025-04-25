package com.example.myapplication.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.db.LegalizacionDatabaseHelper
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewFormFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_new_form, container, false)

        val etNumeroActa = view.findViewById<TextInputEditText>(R.id.etNumeroActa)
        val actvNumeroOrden = view.findViewById<MaterialAutoCompleteTextView>(R.id.etNumeroOrden)
        val actvEmpresa = view.findViewById<MaterialAutoCompleteTextView>(R.id.actvEmpresa)
        val etTecnico = view.findViewById<TextInputEditText>(R.id.etTecnico)
        val actvEstado = view.findViewById<MaterialAutoCompleteTextView>(R.id.actvEstado)
        val etMotivoRetiro = view.findViewById<TextInputEditText>(R.id.etMotivoRetiro)
        val actvClase = view.findViewById<MaterialAutoCompleteTextView>(R.id.actvClase)
        val etMedidor = view.findViewById<TextInputEditText>(R.id.etMedidor)
        val etMarca = view.findViewById<TextInputEditText>(R.id.etMarca)
        val etSerie = view.findViewById<TextInputEditText>(R.id.etSerie)
        val actvProducto = view.findViewById<MaterialAutoCompleteTextView>(R.id.actvProducto)

        val btnGuardar = view.findViewById<Button>(R.id.btnGuardar)
        val btnMateriales = view.findViewById<Button>(R.id.btnMateriales)
        val btnDatosBasicos = view.findViewById<Button>(R.id.btnDatosBasicos)
        val btnNuevo = view.findViewById<Button>(R.id.btnNuevo)

        var empresaSeleccionadaId: Int? = null
        val sharedPref = requireContext().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
        val cedula = sharedPref.getString("cedula", null) ?: ""
        val login = sharedPref.getString("login", null) ?: ""
        Log.d("DEBUG_LOGIN_FRAGMENT", "Valor de login leído en fragmento: '$login'")

        // --- Lógica para empresa ---
        viewLifecycleOwner.lifecycleScope.launch {
            val empresas = withContext(Dispatchers.IO) {
                LegalizacionDatabaseHelper().obtenerEmpresasPorCedula(cedula)
            }
            val nombresEmpresas = empresas.map { it.second }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nombresEmpresas)
            actvEmpresa.setAdapter(adapter)
            actvEmpresa.setOnClickListener { actvEmpresa.showDropDown() }
            actvEmpresa.setOnItemClickListener { _, _, position, _ ->
                empresaSeleccionadaId = empresas[position].first
            }
            if (empresas.size == 1) {
                actvEmpresa.setText(empresas[0].second, false)
                empresaSeleccionadaId = empresas[0].first
            }
        }

        // --- Lógica para órdenes por cédula ---
        viewLifecycleOwner.lifecycleScope.launch {
            val ordenesPorCedula = withContext(Dispatchers.IO) {
                LegalizacionDatabaseHelper().obtenerOrdenesPorCedula(cedula)
            }
            val listaOrdenes = ordenesPorCedula.map { it.idOrden.toString() }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listaOrdenes)
            actvNumeroOrden.setAdapter(adapter)
            actvNumeroOrden.setOnClickListener { actvNumeroOrden.showDropDown() }
            actvNumeroOrden.isFocusable = true
            actvNumeroOrden.isFocusableInTouchMode = true
            actvNumeroOrden.isCursorVisible = false
            actvNumeroOrden.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) actvNumeroOrden.showDropDown()
            }
            if (listaOrdenes.isNotEmpty()) {
                actvNumeroOrden.setText(listaOrdenes[0], false)
                etNumeroActa.setText(listaOrdenes[0])
            }
        }

        // --- Lógica para productos ---
        var productoSeleccionadoId: Long? = null
        viewLifecycleOwner.lifecycleScope.launch {
            val productos = withContext(Dispatchers.IO) {
                LegalizacionDatabaseHelper().obtenerProductos()
            }
            val nombresProductos = productos.map { it.second }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nombresProductos)
            actvProducto.setAdapter(adapter)
            actvProducto.setOnClickListener { actvProducto.showDropDown() }
            actvProducto.setOnItemClickListener { _, _, position, _ ->
                productoSeleccionadoId = productos[position].first
            }
        }

        actvNumeroOrden.setOnItemClickListener { _, _, position, _ ->
            val ordenSeleccionada = actvNumeroOrden.adapter.getItem(position).toString()
            etNumeroActa.setText(ordenSeleccionada)
        }

        btnGuardar.setOnClickListener {
            if (validarCampos(
                    etNumeroActa,
                    actvEmpresa,
                    etTecnico,
                    actvEstado,
                    etMotivoRetiro,
                    actvClase,
                    etMedidor,
                    etMarca,
                    etSerie,
                    actvProducto
                )) {
                if (productoSeleccionadoId == null || productoSeleccionadoId == 0L) {
                    mostrarToastSeguro("Por favor, selecciona un producto válido.")
                    return@setOnClickListener
                }
                Log.d("DEBUG_LOGIN", "Valor de login: '$login'")
                if (login.isEmpty()) {
                    mostrarToastSeguro("Error: usuario no autenticado. Por favor, inicia sesión nuevamente.", Toast.LENGTH_LONG)
                    return@setOnClickListener
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    val resultado = withContext(Dispatchers.IO) {
                        val dbHelper = LegalizacionDatabaseHelper()
                        val numeroOrden = actvNumeroOrden.text.toString()
                        val numeroActa = etNumeroActa.text.toString()
                        val idLegalizacion = dbHelper.obtenerIdLegalizacion(numeroOrden, numeroActa)
                        if (idLegalizacion != null) {
                            val pid = 0
                            val pestado = actvEstado.text.toString().toIntOrNull() ?: 0
                            val pubicacion = 1
                            val pidmaterial = productoSeleccionadoId!!.toInt()
                            val pmarca = etMarca.text.toString()
                            val pserie = etSerie.text.toString()
                            val pusuario = login
                            val pempresa = empresaSeleccionadaId?.toString() ?: ""
                            dbHelper.fxPrLegalizacionSello(
                                pid = pid,
                                pidlegaliza = idLegalizacion.toInt(),
                                pestado = pestado,
                                pubicacion = pubicacion,
                                pidmaterial = pidmaterial,
                                pmarca = pmarca,
                                pserie = pserie,
                                pusuario = pusuario,
                                pempresa = pempresa
                            )
                        } else {
                            null
                        }
                    }
                    if (resultado != null) {
                        mostrarToastSeguro(resultado, Toast.LENGTH_LONG)
                    } else {
                        mostrarToastSeguro("No existe una legalización con ese número de acta y orden.", Toast.LENGTH_LONG)
                    }
                }
            } else {
                mostrarToastSeguro("Por favor, complete todos los campos requeridos.")
            }
        }

        btnMateriales.setOnClickListener {
            mostrarToastSeguro("Materiales presionado")
        }

        btnDatosBasicos.setOnClickListener {
            mostrarToastSeguro("Datos Básicos presionado")
        }

        btnNuevo.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val isConnected = withContext(Dispatchers.IO) {
                    com.example.myapplication.db.PostgresHelper.testConnection()
                }
                val message = if (isConnected) "Conexión exitosa a PostgreSQL" else "Fallo al conectar a PostgreSQL"
                mostrarToastSeguro(message, Toast.LENGTH_LONG)
            }
        }

        return view
    }

    private fun mostrarToastSeguro(mensaje: String, duracion: Int = Toast.LENGTH_SHORT) {
        if (activity != null && isAdded && !isDetached &&
            viewLifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)
        ) {
            Toast.makeText(requireContext(), mensaje, duracion).show()
        }
    }

    private fun validarCampos(
        etNumeroActa: TextInputEditText,
        actvEmpresa: MaterialAutoCompleteTextView,
        etTecnico: TextInputEditText,
        actvEstado: MaterialAutoCompleteTextView,
        etMotivoRetiro: TextInputEditText,
        actvClase: MaterialAutoCompleteTextView,
        etMedidor: TextInputEditText,
        etMarca: TextInputEditText,
        etSerie: TextInputEditText,
        actvProducto: MaterialAutoCompleteTextView
    ): Boolean {
        return !(etNumeroActa.text.isNullOrEmpty() ||
                actvEmpresa.text.isNullOrEmpty() ||
                etTecnico.text.isNullOrEmpty() ||
                actvEstado.text.isNullOrEmpty() ||
                etMotivoRetiro.text.isNullOrEmpty() ||
                actvClase.text.isNullOrEmpty() ||
                etMedidor.text.isNullOrEmpty() ||
                etMarca.text.isNullOrEmpty() ||
                etSerie.text.isNullOrEmpty() ||
                actvProducto.text.isNullOrEmpty())
    }
}