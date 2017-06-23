import rest.Phase6Service

final CardCopier copier = new CardCopier()
copier.copyAll();

class CardCopier {

    final Phase6Service service = new Phase6Service("https://lernen.phase-6.de/")
    File file

    def copyAll() {
        def loginResponse = service.login("fat_jules2@candymail.de", "test12")
        def ownerIdNew = loginResponse.userDnsId
        println "Logged in as ${loginResponse.displayName}"
//        file = new File()

        def subjectIds = service.requestSubjectIds()
//        file.write("Subjects:\n")
//        file << subjectList

        subjectIds.each {
            def subjectIdOrig = it.id
            def ownerIdOrig = it.ownerId
            if (it.ownerId != ownerIdNew) {
                def subjectIdNew = "${subjectIdOrig}-${ownerIdNew}".toString()

                copySubject(ownerIdOrig, subjectIdOrig, ownerIdNew, subjectIdNew)
            }
        }

    }


    public void copySubject(ownerIdOrig, subjectIdOrig, ownerIdNew, subjectIdNew) {

        def subjectOrig = service.requestSubject(ownerIdOrig, subjectIdOrig)
        def subjectNew = subjectOrig
        subjectNew << [
                "name"         : subjectOrig.name + "-" + ownerIdNew,
                "imageId"      : subjectOrig.imageId,
        ]
        service.createOrUpdateSubject(ownerIdNew, subjectIdNew, subjectNew)
        println "Created subject: " + subjectNew.name
        copyUnits(ownerIdOrig, subjectIdOrig, ownerIdNew, subjectIdNew)

    }

    public void copyUnits(ownerIdOrig, String subjectIdOrig, ownerIdNew, subjectIdNew) {
        def unitIdsOrig = service.requestUnitIds(subjectIdOrig)

        int i = 1
        unitIdsOrig.each {
            def unitIdOrig = it.id
            if (it.ownerId == ownerIdOrig) {
                def unitOrig = service.requestUnit(ownerIdOrig, unitIdOrig)
//                file.write("Unit ${ownerIdOrig}:\n")
//                file << subjectList

                def unitNew = unitOrig
                unitNew << [
                        "ownerId"         : ownerIdNew,
                        "subjectIdToOwner": [
                                "id"     : subjectIdNew,
                                "ownerId": ownerIdNew
                        ]
                ]
                def unitIdNew = "${unitIdOrig}-${ownerIdNew}"
                it << ["newId": unitIdNew]
                service.createOrUpdateUnit(ownerIdNew, unitIdNew, unitNew)
                println "Created unit   : " + i + "/" + unitIdsOrig.size() + " ${unitNew.name}"
                i++
                copyCards(subjectIdOrig, ownerIdNew, subjectIdNew, unitIdOrig)
            }
        }
    }

    public void copyCards(subjectIdOrig, ownerIdNew, subjectIdNew, unitIdOrig) {
        def cardList = service.requestCardList(subjectIdOrig, unitIdOrig)
        def i = 1
        cardList.each() {
            def cardIdOrig = it.cardIdToOwner.id
            def cardOrig = it.cardContent
            def cardNew = cardOrig
            def unitIdNew = unitIdOrig + "-" + ownerIdNew

            cardNew.ownerId = ownerIdNew
            cardNew.subjectIdToOwner = ["id": subjectIdNew, "ownerId": ownerIdNew]
            cardNew.unitIdToOwner = ["id": unitIdNew, "ownerId": ownerIdNew]
            def cardId = "${cardIdOrig}-${ownerIdNew}"
            service.createCard(ownerIdNew, cardId, cardNew)
            println("Created card   : " + i + "/" + cardList.size() + " Q:${cardNew.question} A:${cardNew.answer}")
            i++
        }
    }
}