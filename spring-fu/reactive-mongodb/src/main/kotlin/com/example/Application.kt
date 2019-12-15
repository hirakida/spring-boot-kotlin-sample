package com.example

import org.springframework.boot.WebApplicationType
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.logging.LogLevel
import org.springframework.fu.kofu.application
import org.springframework.fu.kofu.mongo.reactiveMongodb
import org.springframework.fu.kofu.webflux.webFlux
import java.time.LocalDateTime

val app = application(WebApplicationType.REACTIVE) {
    logging {
        level = LogLevel.INFO
    }
    beans {
        bean<UserHandler>()
        bean<UserRepository>()
    }
    listener<ApplicationReadyEvent> {
        val userRepository = ref<UserRepository>()
        init(userRepository)
    }
    reactiveMongodb {
        embedded()
    }

    webFlux {
        port = if (profiles.contains("test")) 8181 else 8080
        router {
            val userHandler = ref<UserHandler>()
            GET("/users", userHandler::findAll)
            GET("/users/{id}", userHandler::findById)
        }
        codecs {
            string()
            jackson()
        }
    }
}

fun main() {
    app.run()
}

private fun init(userRepository: UserRepository) {
    repeat(10) {
        val user = User("", "name$it", LocalDateTime.now())
        userRepository.save(user).subscribe()
    }
}
