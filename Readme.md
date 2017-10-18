# PandaBot - Discord Bot
[![Build status](https://ci.appveyor.com/api/projects/status/x8fgdoh7qimanjww/branch/master?svg=true)](https://ci.appveyor.com/project/RedPanda4552/pandabot/branch/master)


A proper readme (probably) coming soon. Until then, crappy abridged readme.

Compile the jar with Maven (Use the Install goal). Use the bigger one (it has dependencies internalized, the barebones one won't run).

Run with java -jar <jarname> <discord-bot-token> <youtube-api-key> <your-discord-user-id>. Brief arg description:

discord-bot-token: Lets PandaBot hook into a Discord Bot user. Required to use the bot.
youtube-api-key: Lets PandaBot search Youtube for videos. If you don't have one of these, you can enter "null" or some garbage here and it will happily fail search attempts.
your-discord-user-id: Tells PandaBot who it's overlord is. The user id here has access to "superuser" commands like reload. 