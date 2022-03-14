package pl.agh.iet.controller

class VideoDetailsControllerIT extends AbstractControllerIT {

    def "stream search should properly filter streams"() {
        when: "3 different streams are created"
        def streamIds = create3DifferentStreams()

        then:
        noExceptionThrown()
        streamIds.size() == 3

        when: "Search by Harry phrase"
        def videos = searchByTitleAndExpectSuccess("Harry")

        then:
        videos.size() == 1

        when: "Search by Star Wars phrase"
        videos = searchByTitleAndExpectSuccess("Star Wars")

        then:
        videos.size() == 2

        when: "Search by Dummy phrase"
        videos = searchByTitleAndExpectSuccess("Dummy")

        then:
        videos.size() == 0

        cleanup:
        streamIds.each { id -> deleteStreamAndExpectSuccess(id) }
    }

    def "when request is invalid return BAD_REQUEST"() {
        when:
        searchWithInvailidRequestAndExpectBadRequest()

        then:
        noExceptionThrown()
    }

    def "when title=#title then return BAD_REQUEST"() {
        when:
        searchWithInvailidRequestAndExpectBadRequest(title)

        then:
        noExceptionThrown()

        where:
        title || _
        ""    || _
        " "   || _
        " "   || _
    }

    private List<String> create3DifferentStreams() {
        def requestList = [
                createStreamRequest("harry_potter_1", "Harry Potter 1", "Magic description"),
                createStreamRequest("star_wars_1", "Star Wars 1", "Force is with you"),
                createStreamRequest("star_wars_2", "Star Wars 2", "Force is with you always")
        ]

        return requestList.collect { request ->

            return createStreamAndExpectOkThenReturnId(request.getName(), request.getTitle(), request.getDescription())
        }
    }
}
