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

@Serializable
object ReportScreen

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