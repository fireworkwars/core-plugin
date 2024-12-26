package foundation.esoteric.fireworkwarscore.util

import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

@Suppress("unused")
class PersistentDataManager(private val plugin: JavaPlugin) {
    fun hasKey(holder: PersistentDataHolder?, key: NamespacedKey?): Boolean {
        if (holder == null) {
            return false
        }

        val pdc = holder.persistentDataContainer
        return pdc.has(key!!)
    }

    fun hasKey(holder: PersistentDataHolder?, key: String): Boolean {
        if (holder == null) {
            return false
        }

        val pdc = holder.persistentDataContainer
        return pdc.has(NamespacedKey(plugin, key))
    }

    fun getStringValue(holder: PersistentDataHolder?, key: NamespacedKey?): String? {
        if (holder == null) {
            return null
        }

        val pdc = holder.persistentDataContainer
        return pdc.get(key!!, PersistentDataType.STRING)
    }

    fun getStringValue(holder: PersistentDataHolder?, key: String): String? {
        if (holder == null) {
            return null
        }

        val pdc = holder.persistentDataContainer
        return pdc.get(NamespacedKey(plugin, key), PersistentDataType.STRING)
    }

    fun getBooleanValue(holder: PersistentDataHolder?, key: NamespacedKey?): Boolean? {
        if (holder == null) {
            return null
        }

        val pdc = holder.persistentDataContainer
        return pdc.getOrDefault(key!!, PersistentDataType.BOOLEAN, false)
    }

    fun getBooleanValue(holder: PersistentDataHolder?, key: String): Boolean? {
        if (holder == null) {
            return null
        }

        val pdc = holder.persistentDataContainer
        return pdc.getOrDefault(NamespacedKey(plugin, key), PersistentDataType.BOOLEAN, false)
    }

    fun getIntValue(holder: PersistentDataHolder?, key: NamespacedKey?): Int? {
        if (holder == null) {
            return null
        }

        val pdc = holder.persistentDataContainer
        return pdc.getOrDefault(key!!, PersistentDataType.INTEGER, 0)
    }

    fun getIntValue(holder: PersistentDataHolder?, key: String): Int? {
        if (holder == null) {
            return null
        }

        val pdc = holder.persistentDataContainer
        return pdc.getOrDefault(NamespacedKey(plugin, key), PersistentDataType.INTEGER, 0)
    }

    fun getIntListValue(holder: PersistentDataHolder?, key: NamespacedKey?): IntArray? {
        if (holder == null) {
            return null
        }

        val pdc = holder.persistentDataContainer
        return pdc.get(key!!, PersistentDataType.INTEGER_ARRAY)
    }

    fun getIntListValue(holder: PersistentDataHolder?, key: String): IntArray? {
        if (holder == null) {
            return null
        }

        val pdc = holder.persistentDataContainer
        return pdc.get(NamespacedKey(plugin, key), PersistentDataType.INTEGER_ARRAY)
    }

    fun getUUIDValue(holder: PersistentDataHolder?, key: NamespacedKey?): UUID? {
        if (holder == null) {
            return null
        }

        val pdc = holder.persistentDataContainer
        val value = pdc.get(key!!, PersistentDataType.STRING)

        return try {
            if (value == null) null else UUID.fromString(value)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun setStringValue(holder: PersistentDataHolder?, key: NamespacedKey, value: String) {
        if (holder == null) {
            throw NullPointerException("Provided PDC holder is null for setStringValue(key=" + key.asString() + ", value=" + value + ")")
        }

        val pdc = holder.persistentDataContainer
        pdc.set(key, PersistentDataType.STRING, value)
    }

    fun setStringValue(holder: PersistentDataHolder?, key: String, value: String) {
        if (holder == null) {
            throw NullPointerException("Provided PDC holder is null for setStringValue(key=$key, value=$value)")
        }

        val pdc = holder.persistentDataContainer
        pdc.set(NamespacedKey(plugin, key), PersistentDataType.STRING, value)
    }

    fun setBooleanValue(holder: PersistentDataHolder?, key: NamespacedKey, value: Boolean) {
        if (holder == null) {
            throw NullPointerException("Provided PDC holder is null for setBooleanValue(key=" + key.asString() + ", value=" + value + ")")
        }

        val pdc = holder.persistentDataContainer
        pdc.set(key, PersistentDataType.BOOLEAN, value)
    }

    fun setBooleanValue(holder: PersistentDataHolder?, key: String, value: Boolean) {
        if (holder == null) {
            throw NullPointerException("Provided PDC holder is null for setBooleanValue(key=$key, value=$value)")
        }

        val pdc = holder.persistentDataContainer
        pdc.set(NamespacedKey(plugin, key), PersistentDataType.BOOLEAN, value)
    }

    fun setIntValue(holder: PersistentDataHolder?, key: NamespacedKey, value: Int) {
        if (holder == null) {
            throw NullPointerException("Provided PDC holder is null for setIntValue(key=" + key.asString() + ", value=" + value + ")")
        }

        val pdc = holder.persistentDataContainer
        pdc.set(key, PersistentDataType.INTEGER, value)
    }

    fun setIntValue(holder: PersistentDataHolder?, key: String, value: Int) {
        if (holder == null) {
            throw NullPointerException("Provided PDC holder is null for setIntValue(key=$key, value=$value)")
        }

        val pdc = holder.persistentDataContainer
        pdc.set(NamespacedKey(plugin, key), PersistentDataType.INTEGER, value)
    }

    fun setIntListValue(holder: PersistentDataHolder?, key: NamespacedKey, value: IntArray) {
        if (holder == null) {
            throw NullPointerException("Provided PDC holder is null for setIntListValue(key=" + key.asString() + ", value=" + value.contentToString() + ")")
        }

        val pdc = holder.persistentDataContainer
        pdc.set(key, PersistentDataType.INTEGER_ARRAY, value)
    }

    fun setIntListValue(holder: PersistentDataHolder?, key: String, value: IntArray) {
        if (holder == null) {
            throw NullPointerException("Provided PDC holder is null for setIntListValue(key=" + key + ", value=" + value.contentToString() + ")")
        }

        val pdc = holder.persistentDataContainer
        pdc.set(NamespacedKey(plugin, key), PersistentDataType.INTEGER_ARRAY, value)
    }

    fun setUUIDValue(holder: PersistentDataHolder?, key: NamespacedKey, value: UUID) {
        if (holder == null) {
            throw NullPointerException("Provided PDC holder is null for setUUIDValue(key=" + key.asString() + ", value=" + value + ")")
        }

        val pdc = holder.persistentDataContainer
        pdc.set(key, PersistentDataType.STRING, value.toString())
    }

    fun setUUIDValue(holder: PersistentDataHolder?, key: String, value: UUID) {
        if (holder == null) {
            throw NullPointerException("Provided PDC holder is null for setUUIDValue(key=$key, value=$value)")
        }

        val pdc = holder.persistentDataContainer
        pdc.set(NamespacedKey(plugin, key), PersistentDataType.STRING, value.toString())
    }
}