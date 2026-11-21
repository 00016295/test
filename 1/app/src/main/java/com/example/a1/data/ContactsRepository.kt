package com.example.a1.data
import android.content.Context
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

object ContactsRepository {
    private val _contacts = mutableListOf<Contact>()
    val contacts: List<Contact> get() = _contacts

    private var isLoaded = false

    suspend fun load(context: Context){
        if (!isLoaded){
            refresh(context)
        }
    }

    suspend fun refresh(context: Context){
        withContext(Dispatchers.IO){
            val fetchedList = mutableListOf<Contact>()
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )

            val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} ASC"

            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY)
                val phoneIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)

                val seenIds = mutableListOf<Long>()

                while (cursor.moveToNext()){
                    val id = cursor.getLong(idIdx)

                    if(seenIds.contains(id)) continue

                    val name = cursor.getString(nameIdx) ?: "No name"
                    val phone = cursor.getString(phoneIdx) ?: ""
                    Log.d("ContactsRepo", "id=$id, name=$name, phone=$phone")
                    seenIds.add(id)
                    fetchedList.add(Contact(id,name,phone))
                }
            }
            _contacts.clear()
            _contacts.addAll(fetchedList)
            isLoaded = true
        }
    }

    fun getById(id:Long): Contact? {
        return _contacts.find { it.id == id}
    }
}