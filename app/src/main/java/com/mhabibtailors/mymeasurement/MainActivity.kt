package com.mhabibtailors.mymeasurement

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.content.Context
import android.util.TypedValue
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.security.SecureRandom
import java.util.Locale

private const val BLUE = 0xFF123F83.toInt()
private const val DEEP_BLUE = 0xFF082653.toInt()
private const val GOLD = 0xFFE2B447.toInt()
private const val PALE_BLUE = 0xFFEAF2FF.toInt()
private const val INK = 0xFF18243A.toInt()

class MainActivity : Activity() {
    private lateinit var db: AppDb
    private var currentRole = ""
    private var currentUserId = -1L
    private var currentName = ""
    private var currentShop = ""
    private var currentTailorId = ""
    private var page: LinearLayout? = null
    private val fields = mutableMapOf<String, EditText>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDb(this)
        showRoleChoice()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun rounded(color: Int, radius: Int = 18, stroke: Int = 0): GradientDrawable =
        GradientDrawable().apply {
            setColor(color); cornerRadius = dp(radius).toFloat()
            if (stroke != 0) setStroke(dp(1), stroke)
        }

    private fun root(title: String, subtitle: String? = null): LinearLayout {
        val scroll = ScrollView(this).apply { setBackgroundColor(Color.WHITE) }
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(28))
        }
        val logo = ImageView(this).apply {
            setImageResource(R.drawable.my_measurement_logo)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        col.addView(logo, LinearLayout.LayoutParams(dp(112), dp(112)).apply { gravity = Gravity.CENTER })
        val heading = TextView(this).apply {
            text = title; textSize = 25f; setTextColor(DEEP_BLUE)
            typeface = Typeface.create("sans-serif", Typeface.BOLD); gravity = Gravity.CENTER
        }
        col.addView(heading, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(7) })
        if (subtitle != null) {
            col.addView(TextView(this).apply {
                text = subtitle; textSize = 14f; setTextColor(0xFF64748B.toInt()); gravity = Gravity.CENTER
            }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(6); bottomMargin = dp(18) })
        } else col.addView(Space(this), LinearLayout.LayoutParams(1, dp(18)))
        scroll.addView(col)
        setContentView(scroll)
        page = col
        return col
    }

    private fun label(text: String) = TextView(this).apply {
        this.text = text; textSize = 14f; setTextColor(INK)
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        setPadding(dp(2), dp(9), dp(2), dp(5))
    }

    private fun button(text: String, action: () -> Unit, primary: Boolean = true): Button =
        Button(this).apply {
            this.text = text; isAllCaps = false; textSize = 15f
            setTextColor(if (primary) Color.WHITE else BLUE)
            background = rounded(if (primary) BLUE else PALE_BLUE, 14, if (primary) 0 else BLUE)
            setPadding(dp(10), dp(7), dp(10), dp(7))
            setOnClickListener { action() }
        }

    private fun addButton(col: LinearLayout, text: String, action: () -> Unit, primary: Boolean = true) {
        col.addView(button(text, action, primary), LinearLayout.LayoutParams(-1, dp(52)).apply {
            topMargin = dp(9)
        })
    }

    private fun input(col: LinearLayout, key: String, hint: String, password: Boolean = false): EditText {
        col.addView(label(hint))
        val e = EditText(this).apply {
            this.hint = hint; textSize = 15f; setSingleLine(true)
            setPadding(dp(13), 0, dp(13), 0); background = rounded(0xFFF7F9FC.toInt(), 12, 0xFFD6DFEC.toInt())
            if (password) inputType = 129
        }
        col.addView(e, LinearLayout.LayoutParams(-1, dp(50)))
        fields[key] = e
        return e
    }

    private fun value(key: String) = fields[key]?.text?.toString()?.trim().orEmpty()
    private fun clearFields() = fields.clear()
    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

    private fun showRoleChoice() {
        clearFields()
        val c = root("My Measurement", "Smart measurement records for your tailoring business")
        addButton(c, "You Are a Tailor", { currentRole = "TAILOR"; showAuth(true) })
        addButton(c, "You Are a Customer", { currentRole = "CUSTOMER"; showAuth(true) }, false)
        c.addView(TextView(this).apply {
            text = "M. Habib Tailors"; textSize = 13f; setTextColor(BLUE); gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(-1, dp(42)).apply { topMargin = dp(18) })
    }

    private fun showAuth(register: Boolean) {
        clearFields()
        val role = if (currentRole == "TAILOR") "Tailor" else "Customer"
        val c = root("$role ${if (register) "Registration" else "Login"}",
            if (register) "Create your account to get started" else "Sign in to your account")
        input(c, "name", "Full Name")
        if (currentRole == "TAILOR") input(c, "shop", "Shop Name (required)")
        input(c, "password", "Password", true)
        if (register) {
            addButton(c, "Create Account", {
                val name = value("name"); val pass = value("password"); val shop = value("shop")
                if (name.isBlank() || pass.length < 6 || (currentRole == "TAILOR" && shop.isBlank())) {
                    toast("Enter your name, shop name if Tailor, and password (at least 6 characters).")
                } else {
                    val id = db.createUser(currentRole, name, shop, pass)
                    if (id == -1L) toast("Account could not be created. Please try another name.")
                    else {
                        currentUserId = id; currentName = name; currentShop = shop
                        currentTailorId = db.tailorId(id)
                        toast("Account created successfully")
                        if (currentRole == "TAILOR") showTailorHome() else showCustomerHome()
                    }
                }
            })
            addButton(c, "Already have an account? Login", { showAuth(false) }, false)
        } else {
            addButton(c, "Login", {
                val name = value("name"); val pass = value("password")
                val user = db.login(currentRole, name, pass)
                if (user == null) toast("Login failed. Check your name and password.")
                else {
                    currentUserId = user.id; currentName = user.name; currentShop = user.shop
                    currentTailorId = user.tailorId
                    if (currentRole == "TAILOR") showTailorHome() else showCustomerHome()
                }
            })
            addButton(c, "Create new account", { showAuth(true) }, false)
        }
        addButton(c, "Back to role selection", { showRoleChoice() }, false)
        c.addView(TextView(this).apply {
            text = "Password is stored as a salted PBKDF2 hash on this device. Cloud authentication is not configured in this starter build."
            textSize = 11f; setTextColor(0xFF6B7280.toInt())
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(16) })
    }

    private fun showTailorHome() {
        val c = root("Hello, $currentName", currentShop)
        val card = TextView(this).apply {
            text = "TAILOR ID  $currentTailorId\nKeep this ID safe for customer searches."
            textSize = 16f; setTextColor(DEEP_BLUE); setPadding(dp(16), dp(16), dp(16), dp(16))
            background = rounded(PALE_BLUE, 16)
        }
        c.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })
        addButton(c, "Add Measurement", { showMeasurementForm() })
        addButton(c, "All Measurements", { showMeasurements() }, false)
        addButton(c, "Search Measurements", { showSearch() }, false)
        addButton(c, "Profile", { showProfile() }, false)
        addButton(c, "Log Out", { currentUserId = -1; showRoleChoice() }, false)
    }

    private fun showMeasurementForm() {
        clearFields()
        val c = root("Add Measurement", "A new serial number is generated automatically")
        input(c, "customer", "Customer Name")
        input(c, "orderDate", "Order Date (YYYY-MM-DD)")
        input(c, "returnDate", "Return Date (YYYY-MM-DD)")
        val keys = listOf("length" to "Length", "shoulder" to "Shoulder", "chest" to "Chest",
            "waist" to "Waist", "hip" to "Hip", "sleeve" to "Sleeve Length",
            "armhole" to "Armhole", "neck" to "Neck", "cuff" to "Cuff",
            "kurta" to "Shirt / Kurta Length", "trouser" to "Trouser / Shalwar Length")
        keys.forEach { (key, title) -> input(c, key, title) }
        input(c, "notes", "Notes")
        addButton(c, "Save Measurement", {
            val customer = value("customer")
            if (customer.isBlank()) { toast("Customer name is required."); return@addButton }
            val map = keys.associate { it.first to value(it.first) }
            val serial = db.addMeasurement(currentUserId, customer, value("orderDate"), value("returnDate"), map, value("notes"))
            if (serial != null) { toast("Measurement saved. Serial: $serial"); showTailorHome() }
            else toast("Could not save measurement.")
        })
        addButton(c, "Cancel", { showTailorHome() }, false)
    }

    private fun showMeasurements() {
        val c = root("All Measurements", currentShop)
        val records = db.measurementsForTailor(currentUserId)
        if (records.isEmpty()) {
            c.addView(TextView(this).apply { text = "No measurements saved yet."; textSize = 16f; setTextColor(INK) })
        } else records.forEach { m ->
            val card = TextView(this).apply {
                text = "Serial: ${m.serial}\nCustomer: ${m.customer}\nOrder: ${m.orderDate.ifBlank { "—" }}   Return: ${m.returnDate.ifBlank { "—" }}\n${m.summary}"
                textSize = 14f; setTextColor(INK); setPadding(dp(14), dp(13), dp(14), dp(13)); background = rounded(PALE_BLUE, 14)
            }
            c.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(9) })
        }
        addButton(c, "Back to Dashboard", { showTailorHome() }, false)
    }

    private fun showSearch() {
        clearFields()
        val c = root("Search Your Measurement", "Starter search checks locally saved records")
        input(c, "customer", "Customer Name")
        input(c, "serial", "Serial Number")
        input(c, "shop", "Tailor Shop Name")
        input(c, "tailorId", "Tailor ID")
        addButton(c, "Search", {
            val found = db.customerSearch(value("customer"), value("serial"), value("shop"), value("tailorId"))
            if (found == null) toast("Measurement not found. Please check your details.")
            else AlertDialog.Builder(this).setTitle("Measurement Found")
                .setMessage("Customer: ${found.customer}\nSerial: ${found.serial}\nShop: ${found.shop}\nTailor ID: ${found.tailorId}\n\n${found.summary}")
                .setPositiveButton("OK", null).show()
        })
        addButton(c, "Back", { showCustomerHome() }, false)
    }

    private fun showCustomerHome() {
        val c = root("Welcome, $currentName", "Find measurements shared by your tailor")
        addButton(c, "Search Your Measurement", { showSearch() })
        addButton(c, "My Measurements", {
            AlertDialog.Builder(this).setTitle("My Measurements")
                .setMessage("Cloud sharing and saved customer records will be added in the next development phase.")
                .setPositiveButton("OK", null).show()
        }, false)
        addButton(c, "Profile", { showProfile() }, false)
        addButton(c, "Log Out", { currentUserId = -1; showRoleChoice() }, false)
        c.addView(TextView(this).apply {
            text = "No measurements have been shared with you yet."; textSize = 13f; setTextColor(0xFF64748B.toInt())
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(20) })
    }

    private fun showProfile() {
        val c = root("Profile", "Your account information")
        val info = if (currentRole == "TAILOR") "Name: $currentName\nShop: $currentShop\nTailor ID: $currentTailorId" else "Name: $currentName\nRole: Customer"
        c.addView(TextView(this).apply {
            text = info; textSize = 17f; setTextColor(INK); setPadding(dp(14), dp(14), dp(14), dp(14)); background = rounded(PALE_BLUE)
        })
        addButton(c, "Back", { if (currentRole == "TAILOR") showTailorHome() else showCustomerHome() }, false)
    }
}

private data class UserInfo(val id: Long, val name: String, val shop: String, val tailorId: String)
private data class MeasurementInfo(val serial: String, val customer: String, val shop: String, val tailorId: String, val orderDate: String, val returnDate: String, val summary: String)

private class AppDb(context: Context) : SQLiteOpenHelper(context, "my_measurement.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT, role TEXT NOT NULL, name TEXT NOT NULL COLLATE NOCASE, shop TEXT NOT NULL DEFAULT '', tailor_id TEXT UNIQUE, salt BLOB NOT NULL, pass_hash BLOB NOT NULL, UNIQUE(role,name))""")
        db.execSQL("""CREATE TABLE measurements(id INTEGER PRIMARY KEY AUTOINCREMENT, tailor_user_id INTEGER NOT NULL, serial TEXT NOT NULL, customer TEXT NOT NULL COLLATE NOCASE, order_date TEXT, return_date TEXT, data TEXT NOT NULL, notes TEXT, UNIQUE(tailor_user_id,serial), FOREIGN KEY(tailor_user_id) REFERENCES users(id))""")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) { /* Future upgrades must use migrations; never drop user data. */ }

    private fun hash(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 120_000, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }

    fun createUser(role: String, name: String, shop: String, password: String): Long {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val cv = ContentValues().apply {
            put("role", role); put("name", name); put("shop", shop)
            if (role == "TAILOR") put("tailor_id", generateTailorId())
            put("salt", salt); put("pass_hash", hash(password, salt))
        }
        return try { writableDatabase.insertOrThrow("users", null, cv) } catch (_: Exception) { -1L }
    }

    private fun generateTailorId(): String {
        repeat(30) {
            val candidate = "T-" + (100000..999999).random()
            val cur = readableDatabase.rawQuery("SELECT 1 FROM users WHERE tailor_id=?", arrayOf(candidate))
            val exists = cur.moveToFirst(); cur.close()
            if (!exists) return candidate
        }
        return "T-" + System.currentTimeMillis().toString().takeLast(9)
    }

    fun login(role: String, name: String, password: String): UserInfo? {
        val c = readableDatabase.rawQuery("SELECT id,name,shop,tailor_id,salt,pass_hash FROM users WHERE role=? AND name=? COLLATE NOCASE", arrayOf(role, name))
        c.use {
            if (!it.moveToFirst()) return null
            val salt = it.getBlob(4); val saved = it.getBlob(5)
            if (!hash(password, salt).contentEquals(saved)) return null
            return UserInfo(it.getLong(0), it.getString(1), it.getString(2), if (it.isNull(3)) "" else it.getString(3))
        }
    }

    fun tailorId(id: Long): String {
        val c = readableDatabase.rawQuery("SELECT tailor_id FROM users WHERE id=?", arrayOf(id.toString()))
        c.use { return if (it.moveToFirst() && !it.isNull(0)) it.getString(0) else "" }
    }

    fun addMeasurement(userId: Long, customer: String, order: String, ret: String, values: Map<String,String>, notes: String): String? {
        val cur = readableDatabase.rawQuery("SELECT COALESCE(MAX(CAST(serial AS INTEGER)),0)+1 FROM measurements WHERE tailor_user_id=?", arrayOf(userId.toString()))
        val next = cur.use { if (it.moveToFirst()) it.getInt(0) else 1 }
        val serial = String.format(Locale.US, "%04d", next)
        val data = values.entries.joinToString("\n") { "${it.key}: ${it.value}" }
        val cv = ContentValues().apply {
            put("tailor_user_id", userId); put("serial", serial); put("customer", customer)
            put("order_date", order); put("return_date", ret); put("data", data); put("notes", notes)
        }
        return try { writableDatabase.insertOrThrow("measurements", null, cv); serial } catch (_: Exception) { null }
    }

    fun measurementsForTailor(userId: Long): List<MeasurementInfo> {
        val out = mutableListOf<MeasurementInfo>()
        val c = readableDatabase.rawQuery("SELECT m.serial,m.customer,u.shop,u.tailor_id,m.order_date,m.return_date,m.data,m.notes FROM measurements m JOIN users u ON u.id=m.tailor_user_id WHERE m.tailor_user_id=? ORDER BY m.id DESC", arrayOf(userId.toString()))
        c.use { while (it.moveToNext()) out.add(MeasurementInfo(it.getString(0), it.getString(1), it.getString(2), if(it.isNull(3)) "" else it.getString(3), it.getString(4) ?: "", it.getString(5) ?: "", it.getString(6) + if ((it.getString(7) ?: "").isNotBlank()) "\nNotes: ${it.getString(7)}" else "")) }
        return out
    }

    fun customerSearch(customer: String, serial: String, shop: String, tailorId: String): MeasurementInfo? {
        if (customer.isBlank() || serial.isBlank() || shop.isBlank() || tailorId.isBlank()) return null
        val c = readableDatabase.rawQuery("""SELECT m.serial,m.customer,u.shop,u.tailor_id,m.order_date,m.return_date,m.data,m.notes
            FROM measurements m JOIN users u ON u.id=m.tailor_user_id
            WHERE m.customer=? COLLATE NOCASE AND m.serial=? AND u.shop=? COLLATE NOCASE AND u.tailor_id=?""",
            arrayOf(customer, serial, shop, tailorId))
        c.use {
            if (!it.moveToFirst()) return null
            val summary = it.getString(6) + if ((it.getString(7) ?: "").isNotBlank()) "\nNotes: ${it.getString(7)}" else ""
            return MeasurementInfo(it.getString(0), it.getString(1), it.getString(2), it.getString(3), it.getString(4) ?: "", it.getString(5) ?: "", summary)
        }
    }
}
