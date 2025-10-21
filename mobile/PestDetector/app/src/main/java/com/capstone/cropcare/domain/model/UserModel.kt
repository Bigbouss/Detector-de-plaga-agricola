package com.capstone.cropcare.domain.model

data class UserModel(
    val uid: String,
    val email: String,
    val name: String,
    val role: UserRole,
    val organizationId: String,
    val organizationName: String,
    val phoneNumber: String? = null,
    val mustChangePassword: Boolean = false
)


enum class UserRole {
    ADMIN,
    WORKER;

    fun hasPermission(permission: Permission): Boolean {
        return when (this) {
            ADMIN -> true
            WORKER -> permission in listOf(
                Permission.SCAN_CROPS,
                Permission.CREATE_REPORT,
                Permission.VIEW_OWN_REPORTS
            )
        }
    }
}

enum class Permission {
    // Admin
    MANAGE_USERS,
    CREATE_ZONES,
    EDIT_ZONES,
    VIEW_ALL_REPORTS,
    GENERATE_ANALYTICS,
    CREATE_INVITATIONS,

    // Worker
    SCAN_CROPS,
    CREATE_REPORT,
    VIEW_OWN_REPORTS
}