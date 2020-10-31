package com.roteswasser.spotifysync.common.extensions

import java.time.Duration


fun Duration.formatAsAgo(): String {
    var remaining = this

    val sb = StringBuilder()
    var appendAllFollowing = false
    if (remaining.toDays() > 0) {
        sb.append(remaining.toDays())
        sb.append(" days ")

        remaining = remaining.minusDays(remaining.toDays())
        appendAllFollowing = true
    }

    if (remaining.toHours() > 0 || appendAllFollowing) {
        sb.append(remaining.toHours())
        sb.append(" hours ")

        remaining = remaining.minusHours(remaining.toHours())
        appendAllFollowing = true
    }

    return if (remaining.toMinutes() > 0 || appendAllFollowing) {
        sb.append(remaining.toMinutes())
        sb.append(" minutes ago")
        sb.toString()
    } else {
        "less than a minute ago"
    }
}