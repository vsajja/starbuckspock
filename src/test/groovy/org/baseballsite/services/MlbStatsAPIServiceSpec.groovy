package org.baseballsite.services

import spock.lang.Shared
import spock.lang.Specification

class MlbStatsAPIServiceSpec extends Specification {
    @Shared
    MlbStatsAPIService mlbStatsAPIService

    def setupSpec() {
        mlbStatsAPIService = new MlbStatsAPIService()
    }

    def "get mlb teams"() {
        when:
        def mlbTeams = mlbStatsAPIService.getMlbTeams()

        mlbTeams.each {
            println it.toString()
        }

        then:
        assert mlbTeams.size() == 30
    }

    def "get mlb roster"() {
        setup:
        def jaysTeamId = 141

        when:
        def blueJays = mlbStatsAPIService.getMlbRoster(jaysTeamId)

        blueJays.each {
            println it.toString()
        }

        then:
        assert blueJays.find { it['nameLast'] == 'Guerrero Jr.' }
    }
}
