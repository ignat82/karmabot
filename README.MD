this is telegram bot project implementing poll-based and toxicity
ratings of chat users

every user can 
- answer to another user message with '+' symbol to
initiate 'chockedness' (btw, i'm not really sure how "душность" is 
translated from russian). the poll appears and depending on
the result will either increase one users 'chockedness' points, or 
desrease another users points.
- answer with '++' symbols to initiate 'toxicity' poll with same 
mechanic as previous one
- send commands to print out current 'chokedness' and 'toxicity' 
ratings
- mention bot in the first word of the message to give him
GigaChat prompt message. Bot will deliver to chat ml answer as his own

bot is deployable to docker via provided docker-compose file.
actually there is Dockerfile to build image from source as well.
frankly speaking the sources are pushed to VM by GitHub 
actions  triggered by MR at repo, and build and deploy are triggered 
as part of the pipeline afterward

bot has to have admin privileges at chat
