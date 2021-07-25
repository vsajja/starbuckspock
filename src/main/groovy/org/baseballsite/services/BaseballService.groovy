package org.baseballsite.services

import com.google.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BaseballService {
    final Logger log = LoggerFactory.getLogger(this.class)

    @Inject
    BaseballService() {
    }
}
