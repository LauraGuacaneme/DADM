package co.edu.unal.directorio

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import co.edu.unal.directorio.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: EmpresaDatabaseHelper
    private lateinit var adapter: EmpresaAdapter
    private var empresas: MutableList<Empresa> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = EmpresaDatabaseHelper(this)
        setupRecyclerView()
        setupSpinner()
        setupListeners()

        cargarEmpresas()
    }

    private fun setupRecyclerView() {
        adapter = EmpresaAdapter(empresas, ::eliminarEmpresa, ::editarEmpresa)
        binding.rvEmpresas.layoutManager = LinearLayoutManager(this)
        binding.rvEmpresas.adapter = adapter
    }

    private fun setupSpinner() {
        val clasificaciones = listOf("Todas", "Consultoría", "Desarrollo a la medida", "Fábrica de software")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, clasificaciones)
        binding.spFiltroClasificacion.adapter = spinnerAdapter

        binding.spFiltroClasificacion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                filtrarEmpresas()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupListeners() {
        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarEmpresas()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.fabAgregar.setOnClickListener {
            val intent = Intent(this, AgregarEditarActivity::class.java)
            startActivity(intent)
        }
    }

    private fun cargarEmpresas() {
        empresas.clear()
        empresas.addAll(dbHelper.getEmpresas())
        adapter.notifyDataSetChanged()
    }

    private fun filtrarEmpresas() {
        val textoBusqueda = binding.etBuscar.text.toString().lowercase()
        val clasificacion = binding.spFiltroClasificacion.selectedItem.toString()

        val empresasFiltradas = dbHelper.getEmpresas().filter {
            val coincideNombre = it.nombre.lowercase().contains(textoBusqueda)
            val coincideClasificacion = clasificacion == "Todas" || it.clasificacion == clasificacion
            coincideNombre && coincideClasificacion
        }

        empresas.clear()
        empresas.addAll(empresasFiltradas)
        adapter.notifyDataSetChanged()
    }

    private fun eliminarEmpresa(empresa: Empresa) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar empresa")
            .setMessage("¿Seguro que deseas eliminar ${empresa.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                dbHelper.deleteEmpresa(empresa.id)
                cargarEmpresas()
                Toast.makeText(this, "Empresa eliminada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun editarEmpresa(empresa: Empresa) {
        val intent = Intent(this, AgregarEditarActivity::class.java).apply {
            putExtra("empresa_id", empresa.id)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        cargarEmpresas()
    }
}
