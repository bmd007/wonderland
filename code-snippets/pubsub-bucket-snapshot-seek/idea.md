pubsub has no order by default, and only 7 days of retention.
if producer puts the latest state (or the whole history of deltas) in a bucket,
say, once a day. each day one jsonl file with the latest state so far.
either each file containing all, or incremental files.
the name of files, should indicate/contain/indicate a snapshot in pubsub topic.
all the state in the bucket can be loaded in the start up of the consumer apps.
what is missing from bucket, can be fetched from pubsub topic,
by creating a subscription with seek to the snapshot matching the name of the latest file in the bucket.

