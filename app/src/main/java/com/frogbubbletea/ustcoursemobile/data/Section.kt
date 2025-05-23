package com.frogbubbletea.ustcoursemobile.data

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class Section(
    val code: String,  // L1
    val classNbr: Int,  // 2041
    val schedules: List<SectionSchedule>,  // Date & time, venue, instructor, TA of a section
    val totalQuota: Quota,  // Quota of a section
    val reservedQuotas: List<ReservedQuota>,  // Reserved quotas of a section
    val remarks: Map<String, String>,  // { Consent: Instructor Consent Required }
): Parcelable
