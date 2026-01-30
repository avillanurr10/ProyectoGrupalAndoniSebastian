@Composable
fun ProfileScreen(email: String, themeViewModel: ThemeViewModel, onLogout: () -> Unit) {
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Default.Person, contentDescription = "Avatar", modifier = Modifier.size(60.dp))
            }
        }
        Text(text = "Perfil de Usuario", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Email", style = MaterialTheme.typography.labelMedium)
                Text(text = email, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        }

        // Switch de Tema de main
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Modo Oscuro")
                Switch(checked = isDarkTheme, onCheckedChange = { themeViewModel.toggleTheme(it) })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // TU BOTÓN DE LOGOUT
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Cerrar Sesión")
        }
    }
}