package co.edu.unal.directorio

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.edu.unal.directorio.databinding.ActivityAgregarEditarBinding

class AgregarEditarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgregarEditarBinding
    private lateinit var dbHelper: EmpresaDatabaseHelper
    private var empresaId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarEditarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = EmpresaDatabaseHelper(this)
        setupSpinner()
        setupListeners()

        empresaId = intent.getLongExtra("empresa_id", -1)
        if (empresaId != -1L) {
            cargarEmpresa(empresaId!!)
        }
    }

    private fun setupSpinner() {
        val clasificaciones = listOf("Consultoría", "Desarrollo a la medida", "Fábrica de software")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, clasificaciones)
        binding.spClasificacion.adapter = spinnerAdapter
    }

    private fun setupListeners() {
        binding.btnGuardar.setOnClickListener {
            guardarEmpresa()
        }
    }

    private fun cargarEmpresa(id: Long) {
        val empresa = dbHelper.getEmpresaById(id)
        empresa?.let {
            binding.etNombreEmpresa.setText(it.nombre)
            binding.etURL.setText(it.url)
            binding.etTelefono.setText(it.telefono)
            binding.etEmail.setText(it.email)
            binding.etProductosServicios.setText(it.productosServicios)
            binding.spClasificacion.setSelection(
                (binding.spClasificacion.adapter as ArrayAdapter<String>).getPosition(it.clasificacion)
            )
        }
    }

    private fun guardarEmpresa() {
        val nombre = binding.etNombreEmpresa.text.toString()
        val url = binding.etURL.text.toString()
        val telefono = binding.etTelefono.text.toString()
        val email = binding.etEmail.text.toString()
        val productos = binding.etProductosServicios.text.toString()
        val clasificacion = binding.spClasificacion.selectedItem.toString()

        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        val empresa = Empresa(
            id = empresaId ?: 0,
            nombre = nombre,
            url = url,
            telefono = telefono,
            email = email,
            productosServicios = productos,
            clasificacion = clasificacion
        )

        if (empresaId == null || empresaId == -1L) {
            dbHelper.addEmpresa(empresa)
            Toast.makeText(this, "Empresa añadida", Toast.LENGTH_SHORT).show()
        } else {
            dbHelper.updateEmpresa(empresa)
            Toast.makeText(this, "Empresa actualizada", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}
