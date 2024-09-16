package com.example.lab07

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.example.database.User
import com.example.database.UserDao
import com.example.database.UserDatabase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenUser() {
    val context = LocalContext.current
    var db: UserDatabase
    var id by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var dataUser by remember { mutableStateOf(listOf<User>()) }
    var selectedUsers by remember { mutableStateOf(mutableSetOf<User>()) }
    var isEditMode by remember { mutableStateOf(false) } // Estado del "Modo Edición"

    db = crearDatabase(context)
    val dao = db.userDao()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Management") },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            dataUser = getUsers(dao)
                        }
                    }) {
                        Icon(Icons.Filled.List, contentDescription = "Listar Usuarios")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        // El código para abrir el formulario de agregar usuario se maneja en la parte inferior
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "añadir")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Spacer(Modifier.height(50.dp))
            TextField(
                value = id,
                onValueChange = { id = it },
                label = { Text("ID (solo lectura)") },
                readOnly = true,
                singleLine = true
            )
            TextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name: ") },
                singleLine = true
            )
            TextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name:") },
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val user = User(0, firstName, lastName)
                    coroutineScope.launch {
                        AgregarUsuario(user = user, dao = dao)
                        dataUser = getUsers(dao)
                    }
                    firstName = ""
                    lastName = ""
                }
            ) {
                Text("Agregar Usuario", fontSize = 16.sp)
            }

            Spacer(Modifier.height(16.dp))

            // Botón para activar/desactivar el "Modo Edición"
            Button(
                onClick = {
                    isEditMode = !isEditMode // Alterna el estado del "Modo Edición"
                }
            ) {
                Text(if (isEditMode) "Salir del Modo Edición" else "Modo Edición", fontSize = 16.sp)
            }

            Spacer(Modifier.height(16.dp))

            // Mostrar usuarios con checkboxes en "Modo Edición"
            if (dataUser.isNotEmpty()) {
                dataUser.forEach { user ->
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        if (isEditMode) {
                            Checkbox(
                                checked = selectedUsers.contains(user),
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        selectedUsers.add(user)
                                    } else {
                                        selectedUsers.remove(user)
                                    }
                                }
                            )
                        }
                        Text(
                            text = "${user.firstName} ${user.lastName}",
                            fontSize = 18.sp
                        )
                    }
                }

                if (isEditMode) {
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                selectedUsers.forEach { user ->
                                    EliminarUsuario(user, dao)
                                }
                                selectedUsers.clear()
                                dataUser = getUsers(dao)
                            }
                        }
                    ) {
                        Text("Eliminar Usuarios Seleccionados", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun crearDatabase(context: Context): UserDatabase {
    return Room.databaseBuilder(
        context,
        UserDatabase::class.java,
        "user_db"
    ).build()
}

suspend fun getUsers(dao: UserDao): List<User> {
    return dao.getAll()
}

suspend fun AgregarUsuario(user: User, dao: UserDao): Unit {
    try {
        dao.insert(user)
    } catch (e: Exception) {
        Log.e("User", "Error: insert: ${e.message}")
    }
}

suspend fun EliminarUsuario(user: User, dao: UserDao) {
    try {
        dao.detele(user)
    } catch (e: Exception) {
        Log.e("User", "ERROR: no es posible eliminar el usuario: ${e.message}")
    }
}
