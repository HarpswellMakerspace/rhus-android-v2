curl http://testflightapp.com/api/builds.json --progress-bar \
 -F file=@bin/Calflora-release.apk \
 -F api_token='c78a3623160611823a1fe0613bc3e678_Nzg4Mjk3MjAxMi0xMi0xNCAxNDoyMzo1NC43Nzk3NjY' \
 -F team_token='e48a64f95f61c4e02fc08fe6d125bba3_MjExMDgxMjAxMy0wNC0xMiAxNzozNjoyMS4xNzE5NzE' \
 -F notes="$1" \
 -F notify=True \
 -F distribution_lists='Android Testers' \
| tee -a "/tmp/testflightuploadapi.log" ; test ${PIPESTATUS[0]} -eq 0
