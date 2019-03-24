(define
	(domain dom_nombre_default)
	(:requirements :strips :typing)
	(:types
		celda - objetc
	)
	(:predicates
		(robot_at  ?c - celda)
		(foto  ?c - celda)
		(taladro  ?c - celda)
	)
	(:action hacer-foto
	 :parameters (?c1 ?c2 - celda  )
	 :precondition
		(and
			(robot_at ?c1)
			(not (= ?c1 ?c2))
		)
	 :effect
		(and
			(foto ?c2)
			(not (robot_at ?c1))
			(robot_at ?c2)
		)
	)
	(:action hacer-taladro
	 :parameters (?c1 ?c2 - celda  )
	 :precondition
		(and
			(robot_at ?c1)
			(not (= ?c1 ?c2))
		)
	 :effect
		(and
			(taladro ?c2)
			(not (robot_at ?c1))
			(robot_at ?c2)
		)
	)
)
