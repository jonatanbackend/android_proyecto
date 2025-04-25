package com.example.myapplication.ui.home

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentDrawingBinding
import com.example.myapplication.db.LegalizacionDatabaseHelper
import com.example.myapplication.db.PostgresHelper
import java.util.Calendar
import java.sql.Date
import java.sql.Time
import java.text.SimpleDateFormat

class DrawingFragment : Fragment() {

    private var _binding: FragmentDrawingBinding? = null
    private val binding get() = _binding!!

    private var empresaSeleccionadaId: Int? = null
    private var empresaFechaInicioContrato: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDrawingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Thread {
            val success = PostgresHelper.testConnection()
            requireActivity().runOnUiThread {
                if (success) {
                    Toast.makeText(requireContext(), "Conexión exitosa a PostgreSQL 🎉", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "Fallo al conectar a PostgreSQL ❌", Toast.LENGTH_LONG).show()
                }
            }
        }.start()

        val exitoFalloOptions = listOf("Éxito", "Fallo")
        val causasOptions = listOf("Causa 1", "Causa 2", "Causa 3")

        val exitoFalloAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, exitoFalloOptions)
        binding.actvExitoFallo.setAdapter(exitoFalloAdapter)

        val causasAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, causasOptions)
        binding.actvCausas.setAdapter(causasAdapter)

        binding.actvExitoFallo.setOnClickListener {
            binding.actvExitoFallo.showDropDown()
        }
        binding.actvCausas.setOnClickListener {
            binding.actvCausas.showDropDown()
        }

        binding.actvExitoFallo.setOnItemClickListener { _, _, position, _ ->
            val seleccion = exitoFalloOptions[position]
            Toast.makeText(requireContext(), "Seleccionaste: $seleccion", Toast.LENGTH_SHORT).show()
        }
        binding.actvCausas.setOnItemClickListener { _, _, position, _ ->
            val seleccion = causasOptions[position]
            Toast.makeText(requireContext(), "Causa: $seleccion", Toast.LENGTH_SHORT).show()
        }

        val sharedPref = requireContext().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
        val cedulaUsuario = sharedPref.getString("cedula", "")?.trim() ?: ""
        Log.d("EMPRESAS_DEBUG", "Cédula usada para consulta: '$cedulaUsuario'")
        Log.d("CEDULA_DEBUG", "Cédula leída en DrawingFragment: '$cedulaUsuario'")

        Toast.makeText(requireContext(), "Cédula actual: $cedulaUsuario", Toast.LENGTH_LONG).show()

        // --- ÓRDENES POR CÉDULA DEL USUARIO LOGUEADO ---
        Thread {
            val ordenesPorCedula = LegalizacionDatabaseHelper().obtenerOrdenesPorCedula(cedulaUsuario)
            requireActivity().runOnUiThread {
                val listaOrdenes = ordenesPorCedula.map { it.idOrden }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listaOrdenes)
                binding.actvNumeroOrden.setAdapter(adapter)
                binding.actvNumeroOrden.setOnClickListener {
                    binding.actvNumeroOrden.showDropDown()
                }
                // SOLO SELECCIÓN, NO EDICIÓN, PERO PERMITIR CLICK Y FOCUS
                binding.actvNumeroOrden.isFocusable = true
                binding.actvNumeroOrden.isFocusableInTouchMode = true
                binding.actvNumeroOrden.isCursorVisible = false
                binding.actvNumeroOrden.setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus) binding.actvNumeroOrden.showDropDown()
                }
                if (listaOrdenes.isNotEmpty()) {
                    binding.actvNumeroOrden.setText(listaOrdenes[0], false)
                }
                Toast.makeText(requireContext(), "Órdenes para tu cédula: ${ordenesPorCedula.size}", Toast.LENGTH_LONG).show()
                Log.d("ORDENES_CEDULA", "Órdenes mostradas: $listaOrdenes")
            }
        }.start()
        // --- FIN BLOQUE ÓRDENES POR CÉDULA ---

        if (cedulaUsuario.isNotBlank()) {
            cargarEmpresaYOrdenesPorCedula(cedulaUsuario)
        }

        // SOLO empresas asociadas al usuario
        Thread {
            val empresas = LegalizacionDatabaseHelper().obtenerEmpresasPorCedula(cedulaUsuario)
            Log.d("EMPRESAS_DEBUG", "Empresas obtenidas: ${empresas.size}")
            empresas.forEach {
                Log.d("EMPRESAS_DEBUG", "Empresa: id=${it.first}, nombre=${it.second}, fecha=${it.third}")
            }
            val nombresEmpresas = empresas.map { it.second }
            requireActivity().runOnUiThread {
                if (nombresEmpresas.isEmpty()) {
                    Toast.makeText(requireContext(), "No hay empresas para mostrar", Toast.LENGTH_LONG).show()
                    Log.w("LEGALIZACION_EMPRESA", "No hay empresas para mostrar")
                } else {
                    Toast.makeText(requireContext(), "Empresas cargadas: ${nombresEmpresas.size}", Toast.LENGTH_SHORT).show()
                    Log.d("LEGALIZACION_EMPRESA", "Empresas cargadas: ${nombresEmpresas.size}")
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nombresEmpresas)
                binding.actvEmpresa.setAdapter(adapter)
                binding.actvEmpresa.setOnClickListener {
                    binding.actvEmpresa.showDropDown()
                }
                binding.actvEmpresa.setOnItemClickListener { _, _, position, _ ->
                    val empresa = empresas[position]
                    Log.d("EMPRESAS_DEBUG", "Seleccionaste empresa: id=${empresa.first}, nombre=${empresa.second}, fecha=${empresa.third}")
                    empresaSeleccionadaId = empresa.first
                    empresaFechaInicioContrato = empresa.third
                    val fecha = empresa.third
                    val fechaStr = fecha?.let { SimpleDateFormat("dd/MM/yyyy").format(it) } ?: ""
                    binding.etFechaInicioContrato.setText(fechaStr)
                    Toast.makeText(requireContext(), "Seleccionaste: ${empresa.second}", Toast.LENGTH_SHORT).show()
                    Log.d("LEGALIZACION_EMPRESA", "Seleccionada empresa: ${empresa.second} (id=${empresa.first}) con fecha inicio: $fechaStr")

                    cargarOrdenesAsignadas()
                }
            }
        }.start()

        binding.etFechaEjecucion.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val fecha = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                    binding.etFechaEjecucion.setText(fecha)
                },
                year, month, day
            )
            datePicker.show()
        }

        binding.btnGuardar.setOnClickListener {
            Thread {
                try {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Verificando conexión...", Toast.LENGTH_SHORT).show()
                        Log.d("LEGALIZACION_GUARDAR", "Verificando conexión a la base de datos")
                    }
                    val connected = PostgresHelper.testConnection()
                    if (!connected) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "No se pudo conectar a la base de datos.", Toast.LENGTH_LONG).show()
                            Log.e("LEGALIZACION_GUARDAR", "No se pudo conectar a la base de datos")
                        }
                        return@Thread
                    }

                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Conexión OK. Intentando guardar...", Toast.LENGTH_SHORT).show()
                        Log.d("LEGALIZACION_GUARDAR", "Conexión OK. Intentando guardar...")
                    }

                    val idEmpresa = empresaSeleccionadaId ?: run {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Selecciona una empresa", Toast.LENGTH_LONG).show()
                            Log.w("LEGALIZACION_VALIDACION", "Intento de guardar sin empresa seleccionada")
                        }
                        return@Thread
                    }
                    val fechaInicioContrato = empresaFechaInicioContrato ?: run {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "No se pudo obtener la fecha de inicio del contrato.", Toast.LENGTH_LONG).show()
                            Log.w("LEGALIZACION_VALIDACION", "No se pudo obtener la fecha de inicio del contrato para la empresa seleccionada")
                        }
                        return@Thread
                    }

                    val numeroOrden = binding.actvNumeroOrden.text.toString().trim()
                    val numeroActa = numeroOrden // El número de acta es igual al número de orden

                    // --- VERIFICACIÓN DE DUPLICADO ---
                    val existe = LegalizacionDatabaseHelper().existeLegalizacion(idEmpresa, numeroActa)
                    if (existe) {
                        requireActivity().runOnUiThread {
                            AlertDialog.Builder(requireContext())
                                .setTitle("Registro duplicado")
                                .setMessage("Ya existe un registro con la misma empresa y número de acta/orden.")
                                .setPositiveButton("Aceptar", null)
                                .show()
                        }
                        return@Thread
                    }

                    val sdfDate = SimpleDateFormat("dd/MM/yyyy")
                    val fechaEjecucionDate = Date(sdfDate.parse(binding.etFechaEjecucion.text.toString()).time)
                    val fechaActual = Date(System.currentTimeMillis())
                    if (fechaEjecucionDate.before(fechaInicioContrato) || fechaEjecucionDate.after(fechaActual)) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "La fecha de ejecución debe estar entre $fechaInicioContrato y hoy.",
                                Toast.LENGTH_LONG
                            ).show()
                            Log.w("LEGALIZACION_VALIDACION", "Fecha ejecución fuera de rango: $fechaEjecucionDate, inicio contrato: $fechaInicioContrato, actual: $fechaActual")
                        }
                        return@Thread
                    }

                    val horaInicial = Time.valueOf("08:00:00")
                    val horaFinal = Time.valueOf("09:00:00")

                    val sharedPref = requireContext().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
                    val usuarioLogueado = sharedPref.getString("usuario", "") ?: ""

                    val helper = LegalizacionDatabaseHelper()
                    val resultado = helper.ejecutarFuncionLegalizacion(
                        pid = 0,
                        pfecha = fechaEjecucionDate,
                        ptecnico = "tecnico1",
                        pplaca = "",
                        porden = numeroOrden,
                        paviso = "",
                        phora_inicial = horaInicial,
                        phora_final = horaFinal,
                        psublinea = 149,
                        pobservacion = binding.etObservacionesTerreno.text.toString(),
                        pacta = numeroActa,
                        pusuario = usuarioLogueado,
                        pempresa = idEmpresa.toString(),
                        ptarifa = 1,
                        pnic = "",
                        pnis = "",
                        pnombre = "",
                        pdireccion = "",
                        pbarrio = "",
                        pmunicipio = 1,
                        psector = 1,
                        ptrafo = "",
                        pnodo = "",
                        pcircuito = "",
                        pestrato = 1,
                        ptorden = 1,
                        ptacta = 1,
                        pefectiva = if (binding.actvExitoFallo.text.toString() == "Éxito") 1 else 0,
                        pzona = 85,
                        pid_orden = 1,
                        pcausal = when (binding.actvCausas.text.toString()) {
                            "Causa 1" -> 1
                            "Causa 2" -> 2
                            "Causa 3" -> 3
                            else -> 0
                        },
                        pnombreatiende = binding.etNombreQuienAtiende.text.toString(),
                        pdoc_atiende = binding.etNumeroDocumento.text.toString()
                    )
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            resultado ?: "Error al guardar",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.d("LEGALIZACION_GUARDAR", "Resultado función: ${resultado ?: "Error al guardar"}")
                    }
                } catch (e: Exception) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "Error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e("LEGALIZACION_ERROR", "Error al guardar: ${e.message}", e)
                    }
                    e.printStackTrace()
                }
            }.start()
        }

    }

    private fun cargarOrdenesAsignadas() {
        val sharedPref = requireContext().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
        val usuarioLogueado = sharedPref.getString("usuario", "") ?: ""
        val empresaId = empresaSeleccionadaId?.toString() ?: return

        Thread {
            val listaOrdenes = LegalizacionDatabaseHelper().obtenerOrdenesAsignadas(usuarioLogueado, empresaId)
            requireActivity().runOnUiThread {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listaOrdenes)
                binding.actvNumeroOrden.setAdapter(adapter)
                binding.actvNumeroOrden.setOnClickListener {
                    binding.actvNumeroOrden.showDropDown()
                }
                // SOLO SELECCIÓN, NO EDICIÓN, PERO PERMITIR CLICK Y FOCUS
                binding.actvNumeroOrden.isFocusable = true
                binding.actvNumeroOrden.isFocusableInTouchMode = true
                binding.actvNumeroOrden.isCursorVisible = false
                binding.actvNumeroOrden.setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus) binding.actvNumeroOrden.showDropDown()
                }
            }
        }.start()
    }

    private fun cargarEmpresaYOrdenesPorCedula(cedula: String) {
        Thread {
            val resultado = LegalizacionDatabaseHelper().obtenerEmpresaYOrdenesPorCedula(cedula)
            requireActivity().runOnUiThread {
                if (resultado != null) {
                    val (empresa, ordenes) = resultado
                    Log.d("CEDULA_AUTO", "Empresa encontrada: ${empresa.nombre} (ID: ${empresa.id})")
                    Log.d("CEDULA_AUTO", "Órdenes encontradas: ${ordenes.size}")
                    ordenes.forEach { Log.d("CEDULA_AUTO", "Orden: $it") }

                    binding.actvEmpresa.setText(empresa.nombre, false)
                    empresaSeleccionadaId = empresa.id
                    empresaFechaInicioContrato = empresa.fechaInicioContrato
                    val fechaStr = empresa.fechaInicioContrato?.let { SimpleDateFormat("dd/MM/yyyy").format(it) } ?: ""
                    binding.etFechaInicioContrato.setText(fechaStr)
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, ordenes)
                    binding.actvNumeroOrden.setAdapter(adapter)
                    if (ordenes.isNotEmpty()) {
                        binding.actvNumeroOrden.setText(ordenes[0], false)
                    } else {
                        Toast.makeText(requireContext(), "No hay órdenes asignadas para tu cédula.", Toast.LENGTH_LONG).show()
                    }
                    binding.actvNumeroOrden.setOnClickListener {
                        binding.actvNumeroOrden.showDropDown()
                    }
                    // SOLO SELECCIÓN, NO EDICIÓN, PERO PERMITIR CLICK Y FOCUS
                    binding.actvNumeroOrden.isFocusable = true
                    binding.actvNumeroOrden.isFocusableInTouchMode = true
                    binding.actvNumeroOrden.isCursorVisible = false
                    binding.actvNumeroOrden.setOnFocusChangeListener { v, hasFocus ->
                        if (hasFocus) binding.actvNumeroOrden.showDropDown()
                    }
                } else {
                    Toast.makeText(requireContext(), "No se encontraron datos para la cédula del usuario", Toast.LENGTH_LONG).show()
                    Log.w("CEDULA_AUTO", "No se encontraron datos para la cédula: $cedula")
                }
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}