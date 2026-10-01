package com.example.scanneurdemdicament

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.scanneurdemdicament.data.local.AppDatabase
import com.example.scanneurdemdicament.data.remote.MedicamentApiService
import com.example.scanneurdemdicament.data.repository.MedicamentRepository
import com.example.scanneurdemdicament.data.repository.PrescriptionRepository
import com.example.scanneurdemdicament.ui.screens.HistoryScreen
import com.example.scanneurdemdicament.ui.screens.PrescriptionsScreen
import com.example.scanneurdemdicament.ui.screens.ScannerScreen
import com.example.scanneurdemdicament.ui.screens.SearchScreen
import com.example.scanneurdemdicament.ui.theme.ScanneurDeMédicamentTheme
import com.example.scanneurdemdicament.ui.viewmodel.AppViewModelFactory
import com.example.scanneurdemdicament.ui.viewmodel.HistoryViewModel
import com.example.scanneurdemdicament.ui.viewmodel.PrescriptionViewModel
import com.example.scanneurdemdicament.ui.viewmodel.ScannerViewModel
import com.example.scanneurdemdicament.ui.viewmodel.SearchViewModel

enum class Screen(val route: String, val title: String, val icon: ImageVector) {
    Scanner("scanner", "Scanner", Icons.Default.QrCodeScanner),
    Prescriptions("prescriptions", "Ordonnances", Icons.AutoMirrored.Filled.ReceiptLong),
    History("history", "Historique", Icons.Default.History),
    Search("search", "Rechercher", Icons.Default.Search)
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        val database = AppDatabase.getInstance(applicationContext)
        val apiService = MedicamentApiService.create()
        val repository = MedicamentRepository(apiService, database.scannedMedicamentDao())
        val prescriptionRepository = PrescriptionRepository(database.prescriptionDao())
        val viewModelFactory = AppViewModelFactory(repository, prescriptionRepository)

        setContent {
            ScanneurDeMédicamentTheme {
                MainAppScreen(factory = viewModelFactory)
            }
        }
    }
}

@Composable
fun MainAppScreen(factory: AppViewModelFactory) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Scanner.route

    val scannerViewModel: ScannerViewModel = viewModel(factory = factory)
    val prescriptionViewModel: PrescriptionViewModel = viewModel(factory = factory)
    val historyViewModel: HistoryViewModel = viewModel(factory = factory)
    val searchViewModel: SearchViewModel = viewModel(factory = factory)

    Scaffold(
        bottomBar = {
            NavigationBar {
                Screen.entries.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) }
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Scanner.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Scanner.route) {
                ScannerScreen(viewModel = scannerViewModel)
            }
            composable(Screen.Prescriptions.route) {
                PrescriptionsScreen(
                    viewModel = prescriptionViewModel,
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = historyViewModel,
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    viewModel = searchViewModel,
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                )
            }
        }
    }
}