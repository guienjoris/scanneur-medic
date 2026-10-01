package com.example.scanneurdemdicament.data.remote

import com.google.gson.annotations.SerializedName

data class MedicamentDto(
    @SerializedName("cis")
    val cis: Long,

    @SerializedName("elementPharmaceutique")
    val name: String?,

    @SerializedName("formePharmaceutique")
    val pharmaceuticalForm: String?,

    @SerializedName("voiesAdministration")
    val administrationRoutes: List<String>?,

    @SerializedName("statusAutorisation")
    val authorizationStatus: String?,

    @SerializedName("typeProcedure")
    val procedureType: String?,

    @SerializedName("etatComercialisation")
    val commercialStatus: String?,

    @SerializedName("dateAMM")
    val ammDate: String?,

    @SerializedName("titulaire")
    val holder: String?,

    @SerializedName("surveillanceRenforcee")
    val reinforcedSurveillance: String?,

    @SerializedName("composition")
    val composition: List<CompositionDto>?,

    @SerializedName("presentation")
    val presentations: List<PresentationDto>?,

    @SerializedName("conditions")
    val conditions: List<String>?
)

data class CompositionDto(
    @SerializedName("codeSubstance")
    val substanceCode: Long?,

    @SerializedName("denominationSubstance")
    val substanceName: String?,

    @SerializedName("dosage")
    val dosage: String?,

    @SerializedName("referenceDosage")
    val dosageReference: String?,

    @SerializedName("natureComposant")
    val componentNature: String?
)

data class PresentationDto(
    @SerializedName("cip7")
    val cip7: Long?,

    @SerializedName("cip13")
    val cip13: Long?,

    @SerializedName("libelle")
    val label: String?,

    @SerializedName("statusAdministratif")
    val administrativeStatus: String?,

    @SerializedName("etatComercialisation")
    val commercialStatus: String?,

    @SerializedName("tauxRemboursement")
    val reimbursementRate: String?,

    @SerializedName("prix")
    val price: Double?,

    @SerializedName("prixPublic")
    val publicPrice: Double?,

    @SerializedName("honorairesDispensation")
    val dispensationFee: Double?
)