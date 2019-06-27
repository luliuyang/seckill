--使用存储过程执行秒杀，主要包括在seckill表的对应记录行减库存和在success_killed表加记录
delimiter $$

create procedure `mydatabase`.`kill_by_procedure`
(in v_seckill_id BIGINT,in v_phone BIGINT,
 in v_kill_time timestamp ,out r_result int )
 begin
  declare insert_cnt int default 0;
  start transaction ;
  insert ignore into success_killed (seckill_id,user_phone,kill_time)
  values (v_seckill_id,v_phone,v_kill_time);
  select row_count into insert_cnt;
  if(insert_cnt=0)
  then
    rollback ;
    r_result=-1;
  elseif (insert_cnt<0)
  then
    rollback ;
    r_result=-2;
  else
    update seckill
    set number=number-1
    where seckill_id=v_seckill_id
      and start_time<v_kill_time
      and end_time>v_kill_time
      and number>0;
    select row_count into insert_cnt;
    if(insert_cnt=0)
    then
      rollback ;
      r_result=0;
    elseif(insert_cnt<0)
    then
      rollback ;
      r_result=-2;
     else
      commit ;
      r_result=1;
    end if;
  end if;
 end
 $$

 delimeter ;

 set @r_result=-3;

call execute_seckill(1000, 13631231234, now(), @r_result);

SELECT @r_result;