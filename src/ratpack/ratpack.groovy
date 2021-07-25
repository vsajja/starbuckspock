import groovy.json.JsonBuilder
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

        get('ping') {
            render '<3'
        }

        prefix('api/v1') {
            prefix('fantasy-baseball') {
                get('mlb/teams') {
                    def mlbTeams = mlbStatsAPIService.getMlbTeams()

                    render new JsonBuilder(mlbTeams).toPrettyString()
                }

                get('mlb/team/rosters') {
                    def mlbTeamId = request.queryParams.mlbTeamId

                    if(mlbTeamId) {
                        def teamRosters = mlbStatsAPIService.getMlbRoster(mlbTeamId)

                        render new JsonBuilder(teamRosters).toPrettyString()
                    }
                    else {
                        clientError(400)
                    }
                }
            }
        }

        files {
            dir 'dist'
            indexFiles 'index.html'
        }
    }
}
