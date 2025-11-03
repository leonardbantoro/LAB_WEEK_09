package com.example.lab_week_09

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import android.widget.Toast
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lab_week_09.ui.theme.LAB_WEEK_09Theme
import com.example.lab_week_09.ui.theme.OnBackgroundItemText
import com.example.lab_week_09.ui.theme.OnBackgroundTitleText
import com.example.lab_week_09.ui.theme.PrimaryTextButton

//Previously we extend AppCompatActivity,
//now we extend ComponentActivity
class MainActivity : ComponentActivity() {
    //Declare a data class called Student
    data class Student(
        var name: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Here, we use setContent instead of setContentView
        setContent {
            //Here, we wrap our content with the theme
            //You can check out the LAB_WEEK_09Theme inside Theme.kt
            LAB_WEEK_09Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    //We use Modifier.fillMaxSize() to make the surface fill the whole screen
                    modifier = Modifier.fillMaxSize(),
                    //We use MaterialTheme.colorScheme.background to get the background color
                    //and set it as the color of the surface
                    color = MaterialTheme.colorScheme.background
                ) {
                    //Don’t forget to change your root from Home() to App()
                    val navController = rememberNavController()
                    App(navController = navController)
                }
            }
        }
    }
}

//Here, we create a composable function called App
//This will be the root composable of the app
@Composable
fun App(navController: NavHostController) {
    //Here, we use NavHost to create a navigation graph
    //We pass the navController as a parameter
    //We also set the startDestination to "home"
    //This means that the app will start with the Home composable
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        //Here, we create a route called "home"
        //We pass the Home composable as a parameter
        //This means that when the app navigates to "home",
        //the Home composable will be displayed
        composable("home") {
            //Here, we pass a lambda function that navigates to "resultContent"
            //and pass the listData as a parameter
            Home { json ->
                navController.currentBackStackEntry?.let { currentEntry ->
                    navController.navigate("resultContent") {
                        launchSingleTop = true
                    }
                    navController.getBackStackEntry("resultContent")
                        .savedStateHandle["listData"] = json
                }
            }
        }

        //Here, we create a route called "resultContent"
        //We pass the ResultContent composable as a parameter
        //This means that when the app navigates to "resultContent",
        //the ResultContent composable will be displayed
        composable("resultContent") { backStackEntry ->
            // ✅ Retrieve JSON data from SavedStateHandle
            val listData = backStackEntry
                .savedStateHandle
                .get<String>("listData")
                .orEmpty()

            //Here, we pass the value of the argument to the ResultContent composable
            ResultContent(listData, navController)
        }
    }
}

//Here, instead of defining it in an XML file,
//we create a composable function called Home
@Composable
fun Home(
    navigateFromHomeToResult: (String) -> Unit
) {
    val moshi = Moshi.Builder()
        .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()

    val type = Types.newParameterizedType(List::class.java, MainActivity.Student::class.java)
    val adapter = moshi.adapter<List<MainActivity.Student>>(type)

    val context = LocalContext.current
    //Here, we create a mutable state list of Student
    val listData = rememberSaveable(
        saver = androidx.compose.runtime.saveable.listSaver(
            save = { list -> list.map { it.name } },
            restore = { names -> names.map { MainActivity.Student(it) }.toMutableStateList() }
        )
    ) {
        mutableStateListOf(
            MainActivity.Student("Tanu"),
            MainActivity.Student("Tina"),
            MainActivity.Student("Tono")
        )
    }
    //Here, we create a mutable state of Student
    var inputField by remember { mutableStateOf(MainActivity.Student("")) }

    //We call the HomeContent composable
    HomeContent(
        listData,
        inputField,
        { input -> inputField = MainActivity.Student(input) },
        {
            if (inputField.name.isNotBlank()) {
                listData.add(inputField)
                inputField = MainActivity.Student("")
            } else {
                Toast.makeText(
                    context,
                    "Please enter a name before submitting!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        },
        {
            if (listData.isNotEmpty()) {
                val json = adapter.toJson(listData.toList())
                navigateFromHomeToResult(json)
            } else {
                Toast.makeText(context, "No students to submit!", Toast.LENGTH_SHORT).show()
            }
        }
    )
}

//Here, we create a composable function called HomeContent
//HomeContent is used to display the content of the Home composable
@Composable
fun HomeContent(
    listData: SnapshotStateList<MainActivity.Student>,
    inputField: MainActivity.Student,
    onInputValueChange: (String) -> Unit,
    onButtonClick: () -> Unit,
    navigateFromHomeToResult: () -> Unit
) {
    //Here, we use LazyColumn to display a list of items lazily
    LazyColumn {
        //Here, we use item to display an item inside the LazyColumn
        item {
            Column(
                //Modifier.padding(16.dp) is used to add padding to the Column
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center, // centers vertically
                horizontalAlignment = Alignment.CenterHorizontally // centers horizontally
            ) {
                //Here, we call the OnBackgroundTitleText UI Element
                OnBackgroundTitleText(text = stringResource(
                    id = R.string.enter_item)
                )

                //Here, we use TextField to display a text input field
                TextField(
                    //Set the value of the input field
                    value = inputField.name,
                    //Set the keyboard type of the input field
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text
                    ),
                    //Set what happens when the value of the input field changes
                    onValueChange = { onInputValueChange(it) }
                )

                Row {
                    //Here, we call the PrimaryTextButton UI Element
                    PrimaryTextButton(text = stringResource(id = R.string.button_click)) {
                        onButtonClick()
                    }

                    //Here, we call the Finish Button to navigate to another page
                    PrimaryTextButton(text = stringResource(id = R.string.button_navigate)) {
                        navigateFromHomeToResult()
                    }
                }
            }
        }

        //Here, we use items to display a list of items inside the LazyColumn
        // ✅ BONUS FEATURE: Tap on name to delete it
        items(listData) { item ->
            Column(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxSize()
                    .clickable { listData.remove(item) }, // <-- Bonus Delete Feature
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //Here, we call the OnBackgroundItemText UI Element
                OnBackgroundItemText(text = item.name)
            }
        }
    }
}

//Here, we create a composable function called ResultContent
//ResultContent accepts a String parameter called listData from the Home composable
//then displays the value of listData to the screen
@Composable
fun ResultContent(listData: String, navController: NavHostController? = null) {
    val moshi = Moshi.Builder()
        .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()

    val type = Types.newParameterizedType(List::class.java, MainActivity.Student::class.java)
    val adapter = moshi.adapter<List<MainActivity.Student>>(type)

    // Safely parse JSON
    val students = remember {
        try {
            adapter.fromJson(listData) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OnBackgroundTitleText(text = "Submitted Students")

        LazyColumn {
            items(students) { student ->
                OnBackgroundItemText(text = student.name)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ✅ BONUS FEATURE: Back to Home button
        PrimaryTextButton(text = "Back to Home") {
            navController?.popBackStack("home", inclusive = false)
        }
    }
}
