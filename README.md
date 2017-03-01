#PandaBot - Discord Bot#

##What it Does##
PandaBot's only current function is to play music. PandaBot uses a slightly modified variant of the music player in [Frederikam's Fredboat Bot for Discord] (https://github.com/Frederikam/FredBoat).

##Requirements##
If you want to use PandaBot on your Discord server, there will be some setup to do, as this bot is only to be self hosted and is not publicly available to invite.
* You will need to have Java 8 installed on the system that is running PandaBot.
* You will need to acquire a Youtube API Key for the music player to work. This is fairly easy to set up via [Google's API Console] (https://console.developers.google.com/). Last time I have checked, it was called "Youtube Data API v3".
* You will need to create a Discord bot user that will be PandaBot's interface to your server. This process is also fairly easy to go through at the [My Apps page on Discord] (https://discordapp.com/developers/applications/me). Specifically, you will be after the bot user's **token**, which is how PandaBot will be able to log in to Discord.
* You will need your Discord server's Guild ID. This is fairly easy to get if you use Discord in a web browser, as it shows up in the address bar of your web browser.
* You will need your Discord User ID. If you enable Developer Mode in Discord (this can be found in your user settings), you can then right click your name in Discord and copy your ID.

##Installation and Operation##
PandaBot binaries are available on the [Releases seciton of this repository] (https://github.com/RedPanda4552/PandaBot/releases). These jar files are shaded and have dependencies internalized. The directory you place it at does not matter.
To run PandaBot, execute the following in a command shell: **java -jar <pandabot jar name>**