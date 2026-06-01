-- KEYS[1] = buffer:{txid}   (list)
-- KEYS[2] = decision:{txid} (string)
-- KEYS[3] = pending         (zset, global)
-- KEYS[4] = error:{txid}    (string flag; if present, force keep regardless of band)
-- ARGV[1] = "keep" | "drop"  (proposed decision from the response-time band)
-- ARGV[2] = txid (zset member)
-- ARGV[3] = decisionTtlSeconds
-- returns: false if another node already decided (skip).
--          otherwise {decision, span1, span2, ...} (spans included only when keep).
local decision = ARGV[1]
if redis.call('GET', KEYS[4]) then
    decision = 'keep'  -- any span in the trace errored -> keep 100%
end
local ok = redis.call('SET', KEYS[2], decision, 'NX', 'EX', ARGV[3])
if not ok then
    return false
end
local spans = redis.call('LRANGE', KEYS[1], 0, -1)
redis.call('DEL', KEYS[1])
redis.call('ZREM', KEYS[3], ARGV[2])
redis.call('DEL', KEYS[4])
local result = {decision}
if decision == 'keep' then
    for i = 1, #spans do
        result[#result + 1] = spans[i]
    end
end
return result
