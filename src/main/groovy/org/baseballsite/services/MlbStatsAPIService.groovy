package org.baseballsite.services

import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MlbStatsAPIService {
    final Logger log = LoggerFactory.getLogger(this.class)

    def MLB_STATS_API_BASE_URL = 'https://statsapi.mlb.com/api/v1'

    def MLB_SPORT_CODE = 1

    // league ids
    def int AL = 103
    def int NL = 104

    // AL division ids
    def AL_EAST = 201
    def AL_CENTRAL = 202
    def AL_WEST = 200

    def NL_EAST = 204
    def NL_CENTRAL = 205
    def NL_WEST = 203

    def LEAGUE_LEVELS = [
            'Independent Leagues',
            'Winter Leagues',
            'Rookie Advanced',
            'Class A Short Season',
            'Class A',
            'Class A Advanced',
            'Double-A',
            'Triple-A',
            'Major League Baseball'
    ]


    def getMlbTeams() {
        String jsonStr = "$MLB_STATS_API_BASE_URL/teams?sportCode=${MLB_SPORT_CODE}".toURL().text

        def teamsObj = new JsonSlurper().parseText(jsonStr)

        def mlbTeams = teamsObj['teams'].findAll {
                    it['league'] &&
                    it['division'] &&
                    it['sport'] &&
                    it['sport']['name'] != 'National Basketball Association'
        }.collect { team ->
            return [
                    'team'           : team['name'],
                    'abbreviation'   : team['abbreviation'],
                    'division'       : team['division']['name'],
                    'league'         : team['league']['name'],
                    'level'          : team['sport']['name'],
                    'firstYearOfPlay': Integer.parseInt(team['firstYearOfPlay']),
                    'divisionId'     : team['division']['id'],
                    'leagueId'       : team['league']['id'],
                    'id'             : team['id']
            ]
        }.findAll { it.level == 'Major League Baseball' }

        return mlbTeams
    }

    def getMlbRoster(def mlbTeamId) {
        log.info("Getting 40 man roster for mlbTeamId: $mlbTeamId")

        String jsonStr = "$MLB_STATS_API_BASE_URL/teams/${mlbTeamId}/roster?rosterType=40Man".toURL().text
        def jsonObj = new JsonSlurper().parseText(jsonStr)
        def teamRoster = []

        log.info("Processing 40 man roster for mlbTeamId: $mlbTeamId")

        teamRoster = jsonObj.roster.collect { player ->
            Integer mlbPlayerId = player['person']['id']
            Integer jerseyNumber = player['jerseyNumber'] != '' ? Integer.parseInt(player['jerseyNumber']) : null
            String playerLink = player['person']['link']

            // use the playerLink to get more player details
            def playerInfo = new JsonSlurper().parseText(
                "https://statsapi.mlb.com/${playerLink}".toURL().text
            )

            String nameFirst = playerInfo['people']['useName'][0]
            String nameLast = playerInfo['people']['lastName'][0]

            log.info("Processing player: $nameFirst $nameLast")

            String birthDate = playerInfo['people']['birthDate'][0]
            Integer age = playerInfo['people']['currentAge'][0]

            String birthCity = playerInfo['people']['birthCity'][0]
            String birthCountry = playerInfo['people']['birthCountry'][0]
            Integer heightFt = Integer.parseInt((playerInfo['people']['height'][0].split(' ')[0] - "'").trim())
            Integer heightInches = Integer.parseInt(playerInfo['people']['height'][0].split(' ')[1] - "\"".trim())
            Integer weight = playerInfo['people']['weight'][0]
            String position = playerInfo['people']['primaryPosition']['abbreviation'][0]
            String mlbDebutDate = playerInfo['people']['mlbDebutDate'][0]

            String pitchHand = playerInfo['people']['pitchHand']['code'][0]
            String bats = playerInfo['people']['batSide']['code'][0]

            return [
                    'mlbTeamId' : mlbTeamId,
                    'mlbPlayerId' : mlbPlayerId,
                    'jerseyNumber': jerseyNumber,
                    'nameFirst'   : nameFirst,
                    'nameLast'    : nameLast,
                    'birthDate'   : birthDate,
                    'age'         : age,
                    'birthCity'   : birthCity,
                    'birthCountry': birthCountry,
                    'heightFt'    : heightFt,
                    'heightInches': heightInches,
                    'weight'      : weight,
                    'position'    : position,
                    'mlbDebutDate': mlbDebutDate,
                    'pitchHand'   : pitchHand,
                    'bats'        : bats
            ]
        }

        return teamRoster
    }
}
