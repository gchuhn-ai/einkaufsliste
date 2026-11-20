package app.einkaufsliste

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ShoppingItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val done: Boolean = false,
)

/**
 * Speichert die Liste lokal auf dem Handy (SharedPreferences als JSON).
 * In Schritt 2 wird das durch die gemeinsame Cloud-Liste (Firebase) ersetzt.
 */
class ListStore(context: Context) {
    private val prefs = context.getSharedPreferences("einkaufsliste", Context.MODE_PRIVATE)

    fun load(): List<ShoppingItem> {
        val json = prefs.getString("items", null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val o = array.getJSONObject(i)
                ShoppingItem(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    done = o.optBoolean("done", false),
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(items: List<ShoppingItem>) {
        val array = JSONArray()
        items.forEach {
            array.put(
                JSONObject()
                    .put("id", it.id)
                    .put("name", it.name)
                    .put("done", it.done)
            )
        }
        prefs.edit().putString("items", array.toString()).apply()
    }
}

/** Zerlegt Gesprochenes wie "Milch und Eier, Brot" in einzelne Artikel. */
fun parseSpoken(spoken: String): List<String> =
    spoken
        .split(Regex("\\s+und\\s+|\\s+sowie\\s+|,", RegexOption.IGNORE_CASE))
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { word -> word.replaceFirstChar { it.uppercase() } }
