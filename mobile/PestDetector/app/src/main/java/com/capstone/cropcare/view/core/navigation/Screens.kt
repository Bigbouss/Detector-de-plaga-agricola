package com.capstone.cropcare.view.core.navigation

import kotlinx.serialization.Serializable

// ----- Screens generales  ----- //

@Serializable
object Splash

@Serializable
object Login



// ----- Screens para trabajador ----- //

@Serializable
object RegisterWorker

@Serializable
object WorkerFlow


@Serializable
object HomeWorker

@Serializable
object HomeHistory

@Serializable
object CamaraScreen

@Serializable
object AnalysisResultScreen

//@Serializable
//object ReportScreen

@Serializable
data class ReportScreen(val sessionId: String, val scanResultId: String)


@Serializable
object QuickReport

// ----- Screens para administrador -----
@Serializable
object RegisterAdmin

@Serializable
object AdminFlow

@Serializable
object HomeAdmin

@Serializable
object ZoneManagement

@Serializable
object ReportManagement

@Serializable
object MetricsAdmin

@Serializable
object InvitationManagement

@Serializable
data class AssignZones(
    val workerId: Int,
    val workerName: String
)

@Serializable
object ScanConfig

@Serializable
data class ScanSession(val sessionId: String)


@Serializable
object ScanZone

@Serializable
data class ScanCrop(val zoneId: String)


