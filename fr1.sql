with popularities as (
   select room,
      round(sum(datediff(checkout, checkin))/180, 2) pop
   from klbuck.lab7_reservations
   group by room),
nextavailable as (
   select room,
      min(checkout) next
   from klbuck.lab7_reservations
   where checkout >= curdate()
   group by room),
staylengths as (
   select room,
      code,
      checkin, checkout,
      datediff(checkout, checkin) length,
      max(checkout) over (partition by room) latest
   from klbuck.lab7_reservations
   where checkout < curdate()),
mostrecent as (
   select room,
      length, 
      checkout
   from staylengths
   where checkout = latest)
select r.*,
   p.pop popularity,
   na.next "next available",
   mr.length "length of last stay",
   mr.checkout "last checkout"
from klbuck.lab7_rooms r
   join popularities p on r.roomcode = p.room
   join nextavailable na on r.roomcode = na.room
   join mostrecent mr on r.roomcode = mr.room
order by p.pop desc;

