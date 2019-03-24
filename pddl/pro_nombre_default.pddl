(define
	(problem pro_nombre_default)
	(:domain dom_nombre_default)
	(:objects
		C1_1 C2_2 - celda
		C11_11 C20_1 - celda
		C7_6 - celda
	)
	(:init
		(robot_at C1_1)
	)
	(:goal	(and
		(foto C2_2)
		(taladro C11_11)
		(foto C20_1)
		(foto C7_6)
		)
	)
)
