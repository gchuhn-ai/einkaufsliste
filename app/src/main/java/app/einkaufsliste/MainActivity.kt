package app.einkaufsliste

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = ListStore(this)
        setContent {
            MaterialTheme {
                ShoppingScreen(store)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(store: ListStore) {
    val context = LocalContext.current
    val list = remember { mutableStateListOf<ShoppingItem>().apply { addAll(store.load()) } }
    var text by remember { mutableStateOf("") }

    fun persist() = store.save(list.toList())

    fun addNames(names: List<String>) {
        names.forEach { list.add(ShoppingItem(name = it)) }
        persist()
    }

    fun addTyped() {
        val name = text.trim()
        if (name.isNotEmpty()) {
            addNames(listOf(name.replaceFirstChar { it.uppercase() }))
            text = ""
        }
    }

    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (spoken != null) {
                val names = parseSpoken(spoken)
                if (names.isNotEmpty()) {
                    addNames(names)
                    Toast.makeText(
                        context,
                        "Hinzugefügt: " + names.joinToString(", "),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun startVoice() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Was soll auf die Liste?")
        }
        try {
            voiceLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "Spracheingabe ist auf diesem Handy nicht verfügbar.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Einkaufsliste") },
                actions = {
                    TextButton(onClick = {
                        list.removeAll { it.done }
                        persist()
                    }) { Text("Erledigte löschen") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { startVoice() }) {
                Text("🎤", fontSize = 24.sp)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Artikel eintippen") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { addTyped() })
                )
                Button(onClick = { addTyped() }) { Text("+") }
            }

            LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
                items(list, key = { it.id }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val i = list.indexOfFirst { it.id == item.id }
                                if (i >= 0) {
                                    list[i] = list[i].copy(done = !list[i].done)
                                    persist()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.done,
                            onCheckedChange = null
                        )
                        Text(
                            text = item.name,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp),
                            textDecoration = if (item.done) TextDecoration.LineThrough else null,
                            color = if (item.done) MaterialTheme.colorScheme.outline
                            else MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = {
                            list.removeAll { it.id == item.id }
                            persist()
                        }) { Text("✕", fontSize = 18.sp) }
                    }
                }
            }
        }
    }
}
