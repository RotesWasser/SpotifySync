package com.roteswasser.spotifysync.controllers

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.view.RedirectView
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
class CustomLoginPage {

    @GetMapping("/login")
    fun customLogin(model: Model): String {
        // TODO: Improve this by find out where to get the real configuration instead of relying on default
        val authorizationRequestBaseUri = OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
        model["loginLink"] = "$authorizationRequestBaseUri/spotify"
        model["isSignOut"] = false

        return "customLogin"
    }

    @GetMapping("/logout")
    fun customLogout(request: HttpServletRequest, response: HttpServletResponse, model: Model): RedirectView {
        model["isSignOut"] = true

        val auth = SecurityContextHolder.getContext().authentication
        auth?.let { SecurityContextLogoutHandler().logout(request, response, auth) }

        return RedirectView("/login?logout")
    }
}