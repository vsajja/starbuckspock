import groovy.json.JsonSlurper
import org.baseballsite.services.BaseballService
import org.baseballsite.services.MlbStatsAPIService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratpack.handling.RequestLogger

import static ratpack.groovy.Groovy.ratpack

final Logger log = LoggerFactory.getLogger(this.class)

ratpack {
    serverConfig {
    }

    bindings {
        bind BaseballService
        bind MlbStatsAPIService
    }

    handlers { BaseballService baseballService, MlbStatsAPIService mlbStatsAPIService ->
        all RequestLogger.ncsa(log)

        all {
            String forwardedProto = 'X-Forwarded-Proto'
            if (request.headers.contains(forwardedProto)
                    && request.headers.get(forwardedProto) != 'https') {
                redirect(301, "https://${request.headers.get('Host')}${request.rawUri}")
            } else {
                next()
            }
        }

        all {
            response.headers.add('Access-Control-Allow-Origin', '*')
            response.headers.add('Access-Control-Allow-Headers', 'Authorization, origin, x-requested-with, content-type')
            response.headers.add('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
            next()
        }

        prefix('api/v1') {
            prefix('fantasy-baseball') {
                get('player-rater') {
                    def playerRatings = file('fantasy-baseball/player_rater_2021.json')

                    render playerRatings
                }
            }
        }

        files {
            dir 'dist'
            indexFiles 'index.html'
        }
    }
}
